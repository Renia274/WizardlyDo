package com.wizardlydo.app.screens.tasks.comps.guide.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun WelcomePage() {
    GuidePageTemplate(
        title = "Welcome to Task Manager!",
        subtitle = "Your magical journey to productivity",
        content = {
            Text(
                "Transform your daily tasks into an epic adventure! As a wizard, completing tasks earns you XP, levels up your character, and unlocks powerful rewards.",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "🎯 Complete tasks to level up",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "⚡ Gain XP and unlock rewards",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "🏆 Build your wizard's power",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    )
}