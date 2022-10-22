package com.marcin.securenotepad

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class TitleActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_title)

        // widgets
        val writePasswordTextView = findViewById<TextView>(R.id.writePasswordTextView)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val goButton = findViewById<Button>(R.id.goButton)

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

        // opening for the first time
        val isOpenForTheFirstTime = !encryptedSharedPreferences.contains("firstTime")

        // writePasswordTextView
        writePasswordTextView.text = when (isOpenForTheFirstTime)
        {
            true  -> "Opened for the first time, set password:"
            false -> "Write password:"
        }

        // goButton
        goButton.setOnClickListener {

            // what was written
            val writtenPassword = passwordEditText.text.toString()

            if (isOpenForTheFirstTime)
            {
                if (writtenPassword.isEmpty())
                {
                    Toast.makeText(this, "Write password", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    // save preferences
                    encryptedSharedPreferencesEditor.putBoolean("firstTime", false)
                    encryptedSharedPreferencesEditor.putString("password", writtenPassword)
                    encryptedSharedPreferencesEditor.putString("note", "new note")
                    encryptedSharedPreferencesEditor.apply()

                    // toast
                    Toast.makeText(this, "New password set", Toast.LENGTH_SHORT).show()

                    // change activity
                    startActivity(Intent(this, NoteActivity::class.java))
                }
            }
            else
            {
                if (writtenPassword.isEmpty())
                {
                    // toast
                    Toast.makeText(this, "Write password", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    if (writtenPassword == encryptedSharedPreferences.getString("password", ""))
                    {
                        // toast
                        Toast.makeText(this, "Good password", Toast.LENGTH_SHORT).show()

                        //change screen
                        startActivity(Intent(this, NoteActivity::class.java))
                    }
                    else
                    {
                        // toast
                        Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}