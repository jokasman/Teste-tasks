package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.TaskEntity
import com.example.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter {
    ALL,
    PENDING,
    COMPLETED
}

private data class TaskFilterState(
    val tasks: List<TaskEntity>,
    val filter: TaskFilter,
    val searchQuery: String,
    val pendingCount: Int,
    val completedCount: Int,
    val totalCount: Int
)

data class TaskUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val searchQuery: String = "",
    val pendingCount: Int = 0,
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val newTaskText: String = "",
    val taskToDelete: TaskEntity? = null,
    val showClearCompletedDialog: Boolean = false,
    val lastDeletedTask: TaskEntity? = null,
    val isLoading: Boolean = false
)

class TaskViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: TaskRepository = TaskRepository(
        AppDatabase.getDatabase(application).taskDao()
    )
) : AndroidViewModel(application) {

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _newTaskText = MutableStateFlow("")
    private val _taskToDelete = MutableStateFlow<TaskEntity?>(null)
    private val _showClearCompletedDialog = MutableStateFlow(false)
    private val _lastDeletedTask = MutableStateFlow<TaskEntity?>(null)

    // Combine database query with filter and search (3 flows)
    private val _tasksAndFilters = combine(
        repository.allTasks,
        _filter,
        _searchQuery
    ) { allTasks, filter, searchQuery ->
        val pendingCount = allTasks.count { !it.isCompleted }
        val completedCount = allTasks.count { it.isCompleted }
        val totalCount = allTasks.size

        val filteredByStatus = when (filter) {
            TaskFilter.ALL -> allTasks
            TaskFilter.PENDING -> allTasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> allTasks.filter { it.isCompleted }
        }

        val filteredTasks = if (searchQuery.isBlank()) {
            filteredByStatus
        } else {
            filteredByStatus.filter { it.title.contains(searchQuery.trim(), ignoreCase = true) }
        }

        TaskFilterState(
            tasks = filteredTasks,
            filter = filter,
            searchQuery = searchQuery,
            pendingCount = pendingCount,
            completedCount = completedCount,
            totalCount = totalCount
        )
    }

    // Combine filter state with UI dialogs and inputs (5 flows)
    val uiState: StateFlow<TaskUiState> = combine(
        _tasksAndFilters,
        _newTaskText,
        _taskToDelete,
        _showClearCompletedDialog,
        _lastDeletedTask
    ) { filterState, newTaskText, taskToDelete, showClearDialog, lastDeleted ->
        TaskUiState(
            tasks = filterState.tasks,
            filter = filterState.filter,
            searchQuery = filterState.searchQuery,
            pendingCount = filterState.pendingCount,
            completedCount = filterState.completedCount,
            totalCount = filterState.totalCount,
            newTaskText = newTaskText,
            taskToDelete = taskToDelete,
            showClearCompletedDialog = showClearDialog,
            lastDeletedTask = lastDeleted,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskUiState(isLoading = true)
    )

    fun onNewTaskTextChange(text: String) {
        _newTaskText.value = text
    }

    fun addTask() {
        val title = _newTaskText.value.trim()
        if (title.isNotEmpty()) {
            viewModelScope.launch {
                repository.insertTask(title)
                _newTaskText.value = ""
            }
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleCompleted(task)
        }
    }

    fun requestDeleteTask(task: TaskEntity) {
        _taskToDelete.value = task
    }

    fun dismissDeleteDialog() {
        _taskToDelete.value = null
    }

    fun confirmDelete() {
        val task = _taskToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteTask(task)
            _lastDeletedTask.value = task
            _taskToDelete.value = null
        }
    }

    fun deleteTaskImmediately(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _lastDeletedTask.value = task
        }
    }

    fun undoDelete() {
        val task = _lastDeletedTask.value ?: return
        viewModelScope.launch {
            repository.insertTaskDirect(task)
            _lastDeletedTask.value = null
        }
    }

    fun clearLastDeleted() {
        _lastDeletedTask.value = null
    }

    fun setFilter(filter: TaskFilter) {
        _filter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openClearCompletedDialog() {
        _showClearCompletedDialog.value = true
    }

    fun dismissClearCompletedDialog() {
        _showClearCompletedDialog.value = false
    }

    fun confirmClearCompleted() {
        viewModelScope.launch {
            repository.clearCompleted()
            _showClearCompletedDialog.value = false
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.getDatabase(application)
                val repo = TaskRepository(db.taskDao())
                return TaskViewModel(application, repo) as T
            }
        }
    }
}
