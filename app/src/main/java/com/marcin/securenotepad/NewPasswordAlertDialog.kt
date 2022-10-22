package com.marcin.securenotepad

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class NewPasswordAlertDialog(val activity : Activity) : Dialog(activity)
{
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_password_alert_dialog)

        // widgets
        val newPasswordEditText = findViewById<EditText>(R.id.newPasswordEditText)
        val cancelButton = findViewById<Button>(R.id.cancelButton)
        val changeButton = findViewById<Button>(R.id.changeButton)

        // cancelButton
        cancelButton.setOnClickListener {

            dismiss()
        }

        // changeButton
        changeButton.setOnClickListener {

            // preferences
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val encryptedSharedPreferences = EncryptedSharedPreferences.create(
                    "encrypted_preferences",
                    masterKeyAlias,
                    activity,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                                                                              )
            val encryptedSharedPreferencesEditor = encryptedSharedPreferences.edit()

            // written password
            val newPassword = newPasswordEditText.text.toString()

            if (newPassword.isEmpty())
            {
                // toast
                Toast.makeText(activity, "Write password", Toast.LENGTH_SHORT).show()
            }
            else
            {
                // save new password
                encryptedSharedPreferencesEditor.putString("password", newPassword)
                encryptedSharedPreferencesEditor.apply()

                // toast
                Toast.makeText(activity, "New password set", Toast.LENGTH_SHORT).show()

                // change screen
                activity.startActivity(Intent(activity, TitleActivity::class.java))
            }
        }
    }
}