package com.wizardlydo.app.screens.tasks.comps.guide.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.screens.tasks.comps.guide.TipCard

@Composable
fun TipsAndTricksPage() {
    GuidePageTemplate(
        title = "Tips & Tricks",
        subtitle = "Maximize your productivity",
        content = {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                TipCard(
                    emoji = "🎯",
                    title = "Set Realistic Goals",
                    tip = "Don't overload yourself! Start with 3-5 tasks per day and build consistency."
                )
                Spacer(modifier = Modifier.height(12.dp))
                TipCard(
                    emoji = "⏰",
                    title = "Use Due Dates Wisely",
                    tip = "Set due dates for important tasks. The countdown helps you prioritize!"
                )
                Spacer(modifier = Modifier.height(12.dp))
                TipCard(
                    emoji = "💪",
                    title = "Complete Tasks Daily",
                    tip = "Build a streak! Consecutive completions boost your stamina regeneration."
                )
                Spacer(modifier = Modifier.height(12.dp))
                TipCard(
                    emoji = "🏆",
                    title = "Prioritize Wisely",
                    tip = "Use High priority for urgent tasks, Medium for important ones, Low for everything else."
                )
                Spacer(modifier = Modifier.height(12.dp))
                TipCard(
                    emoji = "🧙",
                    title = "Level Up Strategically",
                    tip = "Reach Level 30 to unlock special rewards and continue your journey!"
                )
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "You're ready to begin your quest!\n\nMay your tasks be many and your XP plentiful! 🌟",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    )
}