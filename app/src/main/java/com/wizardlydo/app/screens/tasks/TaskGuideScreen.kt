package com.wizardlydo.app.screens.tasks

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.screens.tasks.comps.guide.pages.BottomBarNavigationPage
import com.wizardlydo.app.screens.tasks.comps.guide.pages.CharacterStatsPage
import com.wizardlydo.app.screens.tasks.comps.guide.pages.SearchFeaturePage
import com.wizardlydo.app.screens.tasks.comps.guide.pages.SwipeActionsPage
import com.wizardlydo.app.screens.tasks.comps.guide.pages.TaskFiltersPage
import com.wizardlydo.app.screens.tasks.comps.guide.pages.TaskManagementPage
import com.wizardlydo.app.screens.tasks.comps.guide.pages.TipsAndTricksPage
import com.wizardlydo.app.screens.tasks.comps.guide.pages.WelcomePage
import com.wizardlydo.app.ui.theme.WizardlyDoTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskGuideScreen(
    onFinish: () -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val totalPages = 8

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Manager Guide") },
                actions = {
                    TextButton(onClick = onFinish) {
                        Text("Skip")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Page indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(totalPages) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (index == currentPage)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                    if (index < totalPages - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            // Content
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                },
                label = "guide_page",
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> CharacterStatsPage()
                    2 -> TaskManagementPage()
                    3 -> TaskFiltersPage()
                    4 -> SwipeActionsPage()
                    5 -> SearchFeaturePage()
                    6 -> BottomBarNavigationPage()
                    7 -> TipsAndTricksPage()
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentPage > 0) {
                    TextButton(onClick = { currentPage-- }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (currentPage < totalPages - 1) {
                    Button(onClick = { currentPage++ }) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next")
                    }
                } else {
                    Button(onClick = onFinish) {
                        Text("Get Started!")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskGuideScreenPreview() {
    WizardlyDoTheme {
        TaskGuideScreen(
            onFinish = {}
        )
    }
}