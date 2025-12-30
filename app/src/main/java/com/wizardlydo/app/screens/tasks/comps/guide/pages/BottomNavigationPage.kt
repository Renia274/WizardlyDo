package com.wizardlydo.app.screens.tasks.comps.guide.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.screens.tasks.comps.guide.BottomBarItem

@Composable
fun BottomBarNavigationPage() {
    GuidePageTemplate(
        title = "Bottom Navigation",
        subtitle = "Quick access to all features",
        content = {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                BottomBarItem(
                    iconType = "edit",
                    label = "Edit",
                    description = "Quickly edit any task you chose"
                )
                Spacer(modifier = Modifier.height(12.dp))
                BottomBarItem(
                    iconType = "search",
                    label = "Search",
                    description = "Open search and filter options"
                )
                Spacer(modifier = Modifier.height(12.dp))
                BottomBarItem(
                    iconType = "person",
                    label = "Inventory",
                    description = "View and manage your wizard's items and equipment"
                )
                Spacer(modifier = Modifier.height(12.dp))
                BottomBarItem(
                    iconType = "favorite",
                    label = "Donate",
                    description = "Support the app development (optional)"
                )
                Spacer(modifier = Modifier.height(12.dp))
                BottomBarItem(
                    iconType = "settings",
                    label = "Settings",
                    description = "Customize your experience and preferences"
                )
            }
        }
    )
}