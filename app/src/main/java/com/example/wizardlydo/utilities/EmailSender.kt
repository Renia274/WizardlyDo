package com.example.wizardlydo.utilities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast

class EmailSender(private val context: Context) {
    fun sendEmail(toEmail: String, subject: String, message: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, message)
                // Set your email as the sender
                putExtra(Intent.EXTRA_EMAIL, arrayOf("reniadiol3@gmail.com"))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No email app available", Toast.LENGTH_SHORT).show()
            Log.e("EmailSender", "Failed to open email app: ${e.message}")
        }
    }
}

