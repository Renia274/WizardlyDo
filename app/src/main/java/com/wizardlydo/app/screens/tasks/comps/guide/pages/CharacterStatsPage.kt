package com.wizardlydo.app.screens.tasks.comps.guide.pages

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.screens.tasks.comps.guide.InfoCard

@Composable
fun CharacterStatsPage() {
    GuidePageTemplate(
        title = "Character Stats",
        subtitle = "Monitor your wizard's condition",
        content = {
            InfoCard(
                title = "HP (Health Points)",
                description = "Your wizard's health. Not completing tasks damages your HP. If it reaches 0, you'll need to complete tasks to revive!",
                color = Color(0xFFE57373)
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoCard(
                title = "Stamina",
                description = "Energy for completing tasks. Regenerates over time as you stay consistent with your tasks.",
                color = Color(0xFF81C784)
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoCard(
                title = "XP & Level",
                description = "Complete tasks to gain experience points. Each level unlocks new items, environments and clothing!",
                color = Color(0xFF64B5F6)
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoCard(
                title = "Task Progress",
                description = "Shows how many tasks you've completed toward your next level. Complete all set of tasks to level up!",
                color = Color(0xFFBA68C8)
            )
        }
    )
}