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
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

class NewPasswordAlertDialog(val activity : Activity) : Dialog(activity)
{
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_password_alert_dialog)

        // widgets
        val newPasswordEditText = findViewById<EditText>(R.id.newPasswordEditText)
        val repeatNewPasswordEditText = findViewById<EditText>(R.id.repeatNewPasswordEditText)
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
            val repeatNewPassword = repeatNewPasswordEditText.text.toString()

            if (newPassword.isEmpty())
            {
                // toast
                Toast.makeText(activity, "Write password!", Toast.LENGTH_SHORT).show()
            }
            else if (repeatNewPassword.isEmpty())
            {
                // toast
                Toast.makeText(activity, "Repeat password!", Toast.LENGTH_SHORT).show()
            }
            else if (newPassword != repeatNewPassword)
            {
                // toast
                Toast.makeText(activity, "Different passwords!", Toast.LENGTH_SHORT).show()
            }
            else
            {
                // hashing
                val random = SecureRandom()
                val encoder = Base64.getEncoder()
                val digest = MessageDigest.getInstance("SHA-256")
                val passwordByteArray = newPassword.toByteArray()
                val saltByteArray = ByteArray(32)
                random.nextBytes(saltByteArray)
                val hashByteArray = digest.digest((passwordByteArray + saltByteArray))
                val encodedHash = encoder.encodeToString(hashByteArray)
                val encodedSalt = encoder.encodeToString(saltByteArray)

                // save hash and salt
                encryptedSharedPreferencesEditor.putString("encodedHash", encodedHash)
                encryptedSharedPreferencesEditor.putString("encodedSalt", encodedSalt)
                encryptedSharedPreferencesEditor.apply()

                // toast
                Toast.makeText(activity, "New password set", Toast.LENGTH_SHORT).show()

                // change screen
                activity.startActivity(Intent(activity, TitleActivity::class.java))
            }
        }
    }
}