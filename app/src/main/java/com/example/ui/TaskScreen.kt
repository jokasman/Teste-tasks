package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.EmptyStateView
import com.example.ui.components.TaskFilterChips
import com.example.ui.components.TaskHeader
import com.example.ui.components.TaskInputBar
import com.example.ui.components.TaskItemCard
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.DangerRed

@Composable
fun TaskScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSearchVisible by remember { mutableStateOf(false) }

    // Listen for deleted task to display undo snackbar
    val lastDeletedTask = uiState.lastDeletedTask
    val taskDeletedMessage = stringResource(id = R.string.task_deleted_message)
    val undoLabel = stringResource(id = R.string.undo_action)

    LaunchedEffect(lastDeletedTask) {
        if (lastDeletedTask != null) {
            val result = snackbarHostState.showSnackbar(
                message = taskDeletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearLastDeleted()
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("task_screen"),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("task_snackbar_host")
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Header with App Title, Search, and Status Badge
            TaskHeader(
                pendingCount = uiState.pendingCount,
                completedCount = uiState.completedCount,
                totalCount = uiState.totalCount,
                searchQuery = uiState.searchQuery,
                isSearchVisible = isSearchVisible,
                onToggleSearch = {
                    isSearchVisible = !isSearchVisible
                    if (!isSearchVisible) {
                        viewModel.setSearchQuery("")
                    }
                },
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onClearCompletedClick = { viewModel.openClearCompletedDialog() }
            )

            // Segmented Filter Tabs (Todas, Pendentes, Concluídas)
            if (uiState.totalCount > 0) {
                TaskFilterChips(
                    selectedFilter = uiState.filter,
                    pendingCount = uiState.pendingCount,
                    completedCount = uiState.completedCount,
                    totalCount = uiState.totalCount,
                    onFilterSelected = { viewModel.setFilter(it) }
                )
            }

            // Task List or Empty State
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.tasks.isEmpty() && !uiState.isLoading) {
                    EmptyStateView(
                        filter = uiState.filter,
                        isSearching = uiState.searchQuery.isNotBlank()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("tasks_lazy_column"),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 8.dp,
                            bottom = 12.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = uiState.tasks,
                            key = { it.id }
                        ) { task ->
                            TaskItemCard(
                                task = task,
                                onToggleCompleted = { viewModel.toggleTask(task) },
                                onDeleteClick = { viewModel.requestDeleteTask(task) }
                            )
                        }
                    }
                }
            }

            // Bottom Task Input Field and Action Button
            TaskInputBar(
                text = uiState.newTaskText,
                onTextChange = { viewModel.onNewTaskTextChange(it) },
                onAddTask = { viewModel.addTask() }
            )
        }
    }

    // Confirmation Dialog for Task Deletion
    uiState.taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = {
                Text(
                    text = stringResource(id = R.string.confirm_delete_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.confirm_delete_message, task.title),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.textButtonColors(contentColor = DangerRed),
                    modifier = Modifier.testTag("confirm_delete_dialog_button")
                ) {
                    Text(
                        text = stringResource(id = R.string.confirm_delete_confirm),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissDeleteDialog() },
                    modifier = Modifier.testTag("cancel_delete_dialog_button")
                ) {
                    Text(text = stringResource(id = R.string.cancel_button))
                }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Confirmation Dialog for Clearing Completed Tasks
    if (uiState.showClearCompletedDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearCompletedDialog() },
            title = {
                Text(
                    text = stringResource(id = R.string.clear_completed_action),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = "Deseja eliminar todas as ${uiState.completedCount} tarefas concluídas da sua lista?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmClearCompleted() },
                    colors = ButtonDefaults.textButtonColors(contentColor = DangerRed),
                    modifier = Modifier.testTag("confirm_clear_dialog_button")
                ) {
                    Text(
                        text = stringResource(id = R.string.confirm_delete_confirm),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissClearCompletedDialog() },
                    modifier = Modifier.testTag("cancel_clear_dialog_button")
                ) {
                    Text(text = stringResource(id = R.string.cancel_button))
                }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
