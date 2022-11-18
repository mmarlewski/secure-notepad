package com.marcin.securenotepad

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SettingsActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // widgets
        val deleteDataTextView = findViewById<TextView>(R.id.deleteDataTextView)

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

        // deleteDataTextView
        deleteDataTextView.setOnClickListener {

            // dialog
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle("Delete data?")
            alertDialogBuilder.setPositiveButton("delete") { dialogInterface, int ->

                // remove preferences
                encryptedSharedPreferencesEditor.remove("firstTime")
                encryptedSharedPreferencesEditor.remove("password")
                encryptedSharedPreferencesEditor.remove("note")
                encryptedSharedPreferencesEditor.apply()

                // toast
                Toast.makeText(this, "Data deleted", Toast.LENGTH_SHORT).show()

                // change screen
                startActivity(Intent(this, TitleActivity::class.java))
            }
            alertDialogBuilder.setNegativeButton("cancel") { dialogInterface, int -> }
            alertDialogBuilder.create().show()
        }
    }
}