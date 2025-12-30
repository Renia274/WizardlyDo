package com.wizardlydo.app.screens.tasks.comps.guide.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.screens.tasks.comps.guide.FeatureItem

@Composable
fun TaskManagementPage() {
    GuidePageTemplate(
        title = "Managing Tasks",
        subtitle = "Create and organize your quests",
        content = {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                FeatureItem(
                    title = "Create Tasks",
                    description = "Tap the 'New Task' button to create a new quest. Set title, description, priority, and due date."
                )
                Spacer(modifier = Modifier.height(16.dp))
                FeatureItem(
                    title = "Priority Levels",
                    description = "🔴 High (20 XP) | 🟡 Medium (10 XP) | 🟢 Low (5 XP)"
                )
                Spacer(modifier = Modifier.height(16.dp))
                FeatureItem(
                    title = "Due Dates",
                    description = "Tasks show days remaining. Overdue tasks are marked in red!"
                )
                Spacer(modifier = Modifier.height(16.dp))
                FeatureItem(
                    title = "Categories",
                    description = "Organize tasks by category: Work, Personal, School, Chores, etc."
                )
            }
        }
    )
}