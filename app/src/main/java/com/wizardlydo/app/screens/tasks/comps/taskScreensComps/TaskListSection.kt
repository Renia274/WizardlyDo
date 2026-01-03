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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.data.tasks.Task
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
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
    var lastPageSeen by remember { mutableIntStateOf(currentPage) }
    var showPageChange by remember { mutableStateOf(false) }

    // Track scroll direction for FAB
    val isScrollingDown by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 ||
                    lazyListState.firstVisibleItemScrollOffset > 50
        }
    }

    LaunchedEffect(isScrollingDown) {
        onScrollStateChanged(isScrollingDown)
    }

    // Combine tutorial + real tasks
    val tutorialsFiltered = tutorialTasks.filterNot { it.id in completedTaskIds }
    val realTasksFiltered = tasks.filterNot { it.id in completedTaskIds }
    val allDisplayedTasks = tutorialsFiltered + realTasksFiltered


    LaunchedEffect(allDisplayedTasks.size) {
        editingTaskId = null
    }

    // Show page change notification (only if pagination is active)
    LaunchedEffect(currentPage) {
        if (currentPage != lastPageSeen && totalPages > 1) {
            completedTaskIds = emptySet()
            showPageChange = true

            delay(2000)
            showPageChange = false

        } else if (totalPages == 1) {
            completedTaskIds = emptySet()
        }
    }

    // Scroll detection - ONLY ACTIVE when pagination is enabled (totalPages > 1)
    LaunchedEffect(lazyListState, totalPages) {
        if (totalPages > 1) {
            snapshotFlow {
                val layout = lazyListState.layoutInfo
                val lastVisibleIndex = layout.visibleItemsInfo.lastOrNull()?.index ?: -1
                val firstVisibleIndex = layout.visibleItemsInfo.firstOrNull()?.index ?: -1
                val totalItems = layout.totalItemsCount

                Triple(firstVisibleIndex, lastVisibleIndex, totalItems)
            }
                .debounce(200)
                .filter { (_, _, total) -> total > 0 }
                .collect { (firstIndex, lastIndex, totalItems) ->

                    val lastRealTaskIndex = totalItems - 1

                    if (lastIndex >= lastRealTaskIndex &&
                        currentPage < totalPages &&
                        realTasksFiltered.isNotEmpty()) {
                        onNextPage()
                    }
                    // Reached top - load previous page
                    else if (firstIndex == 0 &&
                        lazyListState.firstVisibleItemScrollOffset < 50 &&
                        currentPage > 1) {
                        onPreviousPage()
                    }
                }
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
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(
                    items = allDisplayedTasks,
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

            // Page change loading indicator (only shows when pagination is active)
            if (totalPages > 1) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showPageChange,
                    enter = androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Card(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .size(60.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(8.dp),
                        shape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

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
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            strokeWidth = 4.dp,
                        )
                        CircularProgressIndicator(
                            progress = { currentPage.toFloat() / totalPages.toFloat() },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp,
                        )
                        // Page number in center
                        Text(
                            text = "$currentPage",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}