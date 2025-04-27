package com.example.wizardlydo.utilities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast

class EmailSender(private val context: Context) {

    /**
     * The simplest way to send an email
     * Opens the user's email app with pre-filled information
     */
    fun sendEmail(toEmail: String, subject: String, message: String) {
        try {
            // Create email intent
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // Only email apps should handle this
                putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            // Start the email app
            context.startActivity(intent)

        } catch (e: Exception) {
            // Show error toast if no email app is available
            Toast.makeText(
                context,
                "No email app available",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("EmailSender", "Failed to open email app: ${e.message}")
        }
    }
}