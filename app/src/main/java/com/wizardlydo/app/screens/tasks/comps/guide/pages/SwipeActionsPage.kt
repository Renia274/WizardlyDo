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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.screens.tasks.comps.guide.SwipeActionDemo

@Composable
fun SwipeActionsPage() {
    GuidePageTemplate(
        title = "Swipe Gestures",
        subtitle = "Quick actions on your tasks",
        content = {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SwipeActionDemo(
                    direction = "Swipe Right →",
                    iconType = "check",
                    color = Color(0xFF4CAF50),
                    description = "Complete the task and earn XP!"
                )
                Spacer(modifier = Modifier.height(20.dp))
                SwipeActionDemo(
                    direction = "Swipe Left ←",
                    iconType = "delete",
                    color = Color(0xFFFF5252),
                    description = "Delete the task (Warning: This damages your HP!)"
                )
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        "⚠️ Deleting tasks causes damage based on priority:\nHigh = 20 HP | Medium = 10 HP | Low = 5 HP",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )
}
