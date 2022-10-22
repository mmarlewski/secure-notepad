package com.marcin.securenotepad

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class NoteActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        // widgets
        val noteEditText = findViewById<EditText>(R.id.noteEditText)

        // preferences
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val encryptedSharedPreferences = EncryptedSharedPreferences.create(
                "encrypted_preferences",
                masterKeyAlias,
                this,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                                                                          )
        val encryptedSharedPreferencesEditor = encryptedSharedPreferences.edit()

        // noteEditText
        noteEditText.addTextChangedListener {
            encryptedSharedPreferencesEditor.putString("note", noteEditText.text.toString())
            encryptedSharedPreferencesEditor.apply()
        }

        noteEditText.setText(encryptedSharedPreferences.getString("note", "could not load note"))
    }

    override fun onBackPressed()
    {
        super.onBackPressed()

        // change screen without back stack
        startActivity(Intent(this, TitleActivity::class.java).apply {
            flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    override fun onCreateOptionsMenu(menu : Menu?) : Boolean
    {
        //return super.onCreateOptionsMenu(menu)

        // options menu
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item : MenuItem) : Boolean
    {
        return when (item.itemId)
        {
            R.id.mainMenuSettings ->
            {
                // change screen
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else                  ->
            {
                super.onOptionsItemSelected(item)
            }
        }
    }
}