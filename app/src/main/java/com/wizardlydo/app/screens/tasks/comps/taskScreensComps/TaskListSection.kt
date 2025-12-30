package com.wizardlydo.app.screens.tasks.comps.taskScreensComps

import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.comps.PageIndicator
import com.wizardlydo.app.data.tasks.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TaskListSection(
    tasks: List<Task>,
    currentPage: Int,
    totalPages: Int,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onCompleteTask: (Int) -> Unit,
    onEditTask: (Int) -> Unit,
    onDeleteTask: (Int) -> Unit,
    tutorialTasks: List<Task> = emptyList(),
    onScrollStateChanged: (isScrollingDown: Boolean) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    var editingTaskId by remember { mutableStateOf<Int?>(null) }
    var completedTaskIds by remember { mutableStateOf(setOf<Int>()) }
    var lastScrollIndex by remember { mutableIntStateOf(0) }
    var isLoadingPage by remember { mutableStateOf(false) }

    // Track scroll direction
    val isScrollingDown by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 ||
                    lazyListState.firstVisibleItemScrollOffset > 50
        }
    }

    // Notify parent about scroll state
    LaunchedEffect(isScrollingDown) {
        onScrollStateChanged(isScrollingDown)
    }

    // Combine and filter tasks
    val allTasks = (tutorialTasks + tasks).filterNot { it.id in completedTaskIds }

    // Reset editing state when tasks change
    LaunchedEffect(allTasks.size) {
        editingTaskId = null
    }

    // Clear completed tasks when page changes
    LaunchedEffect(currentPage) {
        completedTaskIds = emptySet()
        isLoadingPage = false
    }

    // Detect when scrolled to bottom - load next page
    LaunchedEffect(lazyListState.layoutInfo, allTasks.size) {
        val layoutInfo = lazyListState.layoutInfo
        val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val totalItems = allTasks.size

        // If scrolled to last 2 items and not already loading and has more pages
        if (lastVisibleItemIndex >= totalItems - 2 &&
            !isLoadingPage &&
            currentPage < totalPages &&
            totalItems > 0
        ) {
            isLoadingPage = true
            delay(300) // Debounce
            onNextPage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 16.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                userScrollEnabled = true
            ) {
                items(
                    items = allTasks,
                    key = { task -> task.id }
                ) { taskEntity ->
                    val isTutorial = taskEntity.id < 0

                    Box {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = taskEntity.id !in completedTaskIds,
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            TaskItem(
                                taskEntity = taskEntity,
                                onComplete = {
                                    completedTaskIds = completedTaskIds + taskEntity.id
                                    coroutineScope.launch {
                                        delay(300)
                                        onCompleteTask(taskEntity.id)
                                    }
                                    editingTaskId = null
                                },
                                onEdit = {
                                    if (!isTutorial) {
                                        editingTaskId = taskEntity.id
                                        onEditTask(taskEntity.id)
                                    }
                                },
                                onDelete = {
                                    if (!isTutorial) {
                                        completedTaskIds = completedTaskIds + taskEntity.id
                                        coroutineScope.launch {
                                            delay(300)
                                            onDeleteTask(taskEntity.id)
                                        }
                                        editingTaskId = null
                                    }
                                },
                                isEditing = editingTaskId == taskEntity.id
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Page indicator (always shows current page from ViewModel state)
        if (totalPages > 1) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PageIndicator(
                        currentPage = currentPage,
                        totalPages = totalPages
                    )
                }
            }
        }
    }
}