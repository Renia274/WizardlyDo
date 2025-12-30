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
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.screens.tasks.comps.guide.FilterChipDemo

@Composable
fun TaskFiltersPage() {
    GuidePageTemplate(
        title = "Task Filters",
        subtitle = "Find exactly what you need",
        content = {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                FilterChipDemo("ALL", "View all your tasks at once")
                Spacer(modifier = Modifier.height(12.dp))
                FilterChipDemo("ACTIVE", "See only incomplete tasks that need your attention")
                Spacer(modifier = Modifier.height(12.dp))
                FilterChipDemo("COMPLETED", "Review your accomplishments and completed quests")
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        "💡 Tip: Use filters to focus on what matters most!",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    )
}