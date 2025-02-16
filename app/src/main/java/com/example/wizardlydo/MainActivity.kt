package com.example.wizardlydo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.wizardlydo.navigation.graph.NavigationGraph
import com.example.wizardlydo.ui.theme.WizardlyDoTheme
import dagger.hilt.android.AndroidEntryPoint



@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WizardlyDoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavigationGraph()

                }
            }
        }
    }


}

