package com.example.wizardlydo.comps.textFile

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

fun readDonationText(context: Context): List<String> {
    val result = mutableListOf<String>()
    try {
        val inputStream = context.assets.open("donation_text.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            line?.let { if (it.isNotEmpty()) result.add(it) }
        }

        reader.close()
        inputStream.close()
    } catch (e: Exception) {
        result.add("Thank you for supporting our project!")
        result.add("Your donation helps us continue developing amazing features and maintaining this app.")
    }

    return result
}

fun readPayPalUsername(context: Context): String {
    return try {
        val inputStream = context.assets.open("paypal_username.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val username = reader.readLine() ?: ""
        reader.close()
        inputStream.close()
        username
    } catch (e: Exception) {
        ""
    }
}