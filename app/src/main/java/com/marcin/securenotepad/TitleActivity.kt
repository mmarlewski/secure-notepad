package com.marcin.securenotepad

import android.content.Intent
import android.os.Bundle
import android.util.Base64.decode
import android.util.Base64.encodeToString
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

class TitleActivity : AppCompatActivity()
{
    var wrongPasswords = 1
    var isWaiting = false
    var waitStart = Calendar.getInstance().time
    val waitTime = 10000
    val maxAttempts = 3

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_title)

        // widgets
        val writePasswordTextView = findViewById<TextView>(R.id.writePasswordTextView)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val repeatPasswordEditText = findViewById<EditText>(R.id.repeatPasswordEditText)
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

        // repeatPasswordEditText
        repeatPasswordEditText.visibility = if (isOpenForTheFirstTime) View.VISIBLE else View.INVISIBLE

        // goButton
        goButton.setOnClickListener {

            // isWaiting
            if (isWaiting)
            {
                if (Calendar.getInstance().time.time - waitStart.time > waitTime)
                {
                    isWaiting = false
                }
                else
                {
                    Toast.makeText(this, "Wait before another attempt!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // written password
            val password = passwordEditText.text.toString()
            val repeatPassword = repeatPasswordEditText.text.toString()

            if (isOpenForTheFirstTime)
            {
                if (password.isEmpty())
                {
                    // toast
                    Toast.makeText(this, "Write password!", Toast.LENGTH_SHORT).show()
                }
                else if (repeatPassword.isEmpty())
                {
                    // toast
                    Toast.makeText(this, "Repeat password!", Toast.LENGTH_SHORT).show()
                }
                else if (password != repeatPassword)
                {
                    // toast
                    Toast.makeText(this, "Different passwords!", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    // hashing
                    val random = SecureRandom()
                    val encoder = Base64.getEncoder()
                    val digest = MessageDigest.getInstance("SHA-256")
                    val passwordByteArray = password.toByteArray()
                    val saltByteArray = ByteArray(32)
                    random.nextBytes(saltByteArray)
                    val hashByteArray = digest.digest((passwordByteArray + saltByteArray))
                    val encodedHash = encoder.encodeToString(hashByteArray)
                    val encodedSalt = encoder.encodeToString(saltByteArray)

                    // save preferences
                    encryptedSharedPreferencesEditor.putBoolean("firstTime", false)
                    encryptedSharedPreferencesEditor.putString("encodedHash", encodedHash)
                    encryptedSharedPreferencesEditor.putString("encodedSalt", encodedSalt)
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
                if (password.isEmpty())
                {
                    // toast
                    Toast.makeText(this, "Write password!", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    // hashing
                    val decoder = Base64.getDecoder()
                    val digest = MessageDigest.getInstance("SHA-256")
                    val encodedHash = encryptedSharedPreferences.getString("encodedHash", "")
                    val encodedSalt = encryptedSharedPreferences.getString("encodedSalt", "")
                    val hashByteArray = decoder.decode(encodedHash)
                    val saltByteArray = decoder.decode(encodedSalt)
                    val enteredPasswordByteArray = password.toByteArray()
                    val enteredHashByteArray = digest.digest(enteredPasswordByteArray + saltByteArray)

                    if (hashByteArray contentEquals enteredHashByteArray)
                    {
                        // toast
                        Toast.makeText(this, "Good password", Toast.LENGTH_SHORT).show()

                        // wrongPasswords
                        wrongPasswords = 1

                        //change screen
                        startActivity(Intent(this, NoteActivity::class.java))
                    }
                    else
                    {
                        // toast
                        Toast.makeText(this, "Wrong password for $wrongPasswords. time!", Toast.LENGTH_SHORT).show()

                        // clear password
                        passwordEditText.text.clear()

                        // increment
                        wrongPasswords++

                        // trigger wait
                        if (wrongPasswords > maxAttempts)
                        {
                            wrongPasswords = 1
                            isWaiting = true
                            waitStart = Calendar.getInstance().time

                            // toast
                            Toast.makeText(this, "Wait $waitTime milliseconds!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}