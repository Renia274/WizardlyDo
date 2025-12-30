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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.screens.tasks.comps.guide.IconDescriptionRow

@Composable
fun SearchFeaturePage() {
    GuidePageTemplate(
        title = "Search & Filter",
        subtitle = "Find tasks instantly",
        content = {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                IconDescriptionRow(
                    iconType = "search",
                    title = "Search Bar",
                    description = "Type keywords to find specific tasks quickly"
                )
                Spacer(modifier = Modifier.height(16.dp))
                IconDescriptionRow(
                    iconType = "filter",
                    title = "Filter Options",
                    description = "Filter by priority (High/Medium/Low) and task type (All/Active/Completed)"
                )
                Spacer(modifier = Modifier.height(16.dp))
                IconDescriptionRow(
                    iconType = "check",
                    title = "Tap Icon",
                    description = "Open the filter menu to see priority and type filters"
                )
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Text(
                        "💡 Combine search with filters for precise results!",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )
}