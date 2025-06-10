package com.wizardlydo.app.comps.textFile

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


data class Level30DialogStrings(
    val title: String,
    val congratulations: String,
    val mainBody: String,
    val secondaryTitle: String,
    val featuresIntro: String,
    val features: List<String>,
    val continueButton: String,
    val supportButton: String,
    val footer: String
) {
    companion object {
        fun fromText(text: String): Level30DialogStrings {
            val lines = text.trimIndent().lines().filter { it.isNotBlank() }

            // Parse the text to extract different parts
            return Level30DialogStrings(
                title = lines[0],
                congratulations = lines[1],
                mainBody = lines[2],
                secondaryTitle = lines[3],
                featuresIntro = lines[4],
                features = lines.subList(5, 11),
                continueButton = lines[11],
                supportButton = lines[12],
                footer = lines[13]
            )
        }
    }
}

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



suspend fun loadTextFromAssets(context: Context, fileName: String): String {
    return withContext(Dispatchers.IO) {
        try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }
}

@Composable
fun rememberLevel30Strings(context: Context): Level30DialogStrings? {
    var strings by remember { mutableStateOf<Level30DialogStrings?>(null) }

    LaunchedEffect(Unit) {
        val text = loadTextFromAssets(context, "level_30_dialog_strings.txt")
        if (text.isNotEmpty()) {
            strings = Level30DialogStrings.fromText(text)
        }
    }

    return strings
}
