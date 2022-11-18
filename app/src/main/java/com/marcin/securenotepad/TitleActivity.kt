package com.marcin.securenotepad

import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.util.*

class TitleActivity : AppCompatActivity()
{
    val cancellationSignal = CancellationSignal()
    val authenticationCallback = object : BiometricPrompt.AuthenticationCallback()
    {
        override fun onAuthenticationError(errorCode : Int, errString : CharSequence?)
        {
            super.onAuthenticationError(errorCode, errString)
            Toast.makeText(this@TitleActivity, "Error", Toast.LENGTH_SHORT).show()
        }

        override fun onAuthenticationFailed()
        {
            super.onAuthenticationFailed()
            Toast.makeText(this@TitleActivity, "Failed", Toast.LENGTH_SHORT).show()
        }

        override fun onAuthenticationHelp(helpCode : Int, helpString : CharSequence?)
        {
            super.onAuthenticationHelp(helpCode, helpString)
            Toast.makeText(this@TitleActivity, "Help", Toast.LENGTH_SHORT).show()
        }

        override fun onAuthenticationSucceeded(result : BiometricPrompt.AuthenticationResult?)
        {
            super.onAuthenticationSucceeded(result)
            Toast.makeText(this@TitleActivity, "Success", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@TitleActivity, NoteActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_title)

        // widgets
        val textView = findViewById<TextView>(R.id.textView)
        val goButton = findViewById<Button>(R.id.goButton)

        // checks
        if (!(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isKeyguardSecure)
        {
            Toast.makeText(this, "Error: Fingerprint authentication not enabled in settings", Toast.LENGTH_SHORT).show()
        }
        else if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_BIOMETRIC)
                != PackageManager.PERMISSION_GRANTED
        )
        {
            Toast.makeText(this, "Error: Fingerprint authentication not permitted in manifest", Toast.LENGTH_SHORT).show()
        }
        else if (!packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT))
        {
            Toast.makeText(this, "Error: Feature fingerprint lacking", Toast.LENGTH_SHORT).show()
        }
        else
        {
            Toast.makeText(this, "Everything ok", Toast.LENGTH_SHORT).show()
        }

        // textView
        textView.text = "Use biometrics"

        // cancellationSignal
        cancellationSignal.setOnCancelListener {

            Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
        }

        // goButton
        goButton.setOnClickListener {

            // biometricPrompt
            val biometricPrompt = BiometricPrompt.Builder(this)
                    .setTitle("Use your finger")
                    .setSubtitle("real or emulated")
                    .setDescription("To access the note, use your registered fingerprint")
                    .setNegativeButton("Cancel", this.mainExecutor, DialogInterface.OnClickListener { dialogInterface, i ->
                        Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                    })
                    .build()

            biometricPrompt.authenticate(cancellationSignal, mainExecutor, authenticationCallback)
        }
    }
}