package com.wizardlydo.app

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.wizardlydo.app.navigation.graph.NavigationGraph
import com.wizardlydo.app.ui.theme.WizardlyDoTheme

class MainActivity : ComponentActivity() {
    private var preferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)

        setContent {
            // Make dark mode reactive to preference changes
            var isDarkMode by remember {
                mutableStateOf(
                    sharedPreferences.getBoolean(
                        "dark_mode",
                        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
                    )
                )
            }

            // Listen for preference changes within Compose
            DisposableEffect(Unit) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                    if (key == "dark_mode") {
                        isDarkMode = prefs.getBoolean("dark_mode", false)
                    }
                }

                sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
                preferenceChangeListener = listener

                onDispose {
                    sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
                    preferenceChangeListener = null
                }
            }

            WizardlyDoTheme(
                darkTheme = isDarkMode
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        NavigationGraph()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceChangeListener?.let { listener ->
            val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}