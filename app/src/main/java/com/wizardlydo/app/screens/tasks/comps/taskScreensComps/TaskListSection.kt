package com.wizardlydo.app.screens.tasks.comps.taskScreensComps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wizardlydo.app.comps.PageIndicator
import com.wizardlydo.app.data.tasks.Task

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
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
    onNavigationBarVisibilityChange: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = 0,
        initialFirstVisibleItemScrollOffset = 0
    )

    // Track loading state for pagination
    var isLoadingPage by remember { mutableStateOf(false) }

    // Detect scroll position to show/hide FAB and BottomBar
    val navigationVisibilityState = remember { mutableStateOf(true) }

    LaunchedEffect(lazyListState) {
        var previousPendingFirstIndex = 0
        snapshotFlow { lazyListState.firstVisibleItemIndex }.collect { currentFirstIndex ->
            when {
                currentFirstIndex > previousPendingFirstIndex -> {
                    // Scrolling down - hide FAB and BottomBar
                    if (navigationVisibilityState.value) {
                        navigationVisibilityState.value = false
                        onNavigationBarVisibilityChange(false)
                    }
                }

                currentFirstIndex < previousPendingFirstIndex -> {
                    // Scrolling up - show FAB and BottomBar
                    if (!navigationVisibilityState.value) {
                        navigationVisibilityState.value = true
                        onNavigationBarVisibilityChange(true)
                    }
                }
            }
            previousPendingFirstIndex = currentFirstIndex
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                userScrollEnabled = true
            ) {
                items(tasks) { taskEntity ->
                    TaskItem(
                        taskEntity = taskEntity,
                        onComplete = { onCompleteTask(taskEntity.id) },
                        onEdit = { onEditTask(taskEntity.id) },
                        onDelete = { onDeleteTask(taskEntity.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Auto-load next page when reaching bottom
            if (tasks.isNotEmpty()) {
                LaunchedEffect(lazyListState) {
                    snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                        .distinctUntilChanged()
                        .collect { lastVisibleIndex ->
                            if (lastVisibleIndex == tasks.size - 1 && !isLoadingPage && currentPage < totalPages) {
                                isLoadingPage = true
                                onNextPage()
                                delay(500)
                                isLoadingPage = false
                            }
                        }
                }

                // Show loading indicator when fetching next page
                if (isLoadingPage && currentPage < totalPages) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 40.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentPage > 1) {
                                coroutineScope.launch {
                                    onPreviousPage()
                                }
                            }
                        },
                        enabled = currentPage > 1
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous page",
                            tint = if (currentPage > 1)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }

                    PageIndicator(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        modifier = Modifier.weight(1f),
                        showSwipeHint = true
                    )

                    IconButton(
                        onClick = {
                            if (currentPage < totalPages) {
                                coroutineScope.launch {
                                    onNextPage()
                                }
                            }
                        },
                        enabled = currentPage < totalPages
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next page",
                            tint = if (currentPage < totalPages)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}
