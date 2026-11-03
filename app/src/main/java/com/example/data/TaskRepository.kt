package com.example.data

import kotlinx.coroutines.flow.Flow

/**
 * Repositório que abstrai as operações na base de dados local.
 */
class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun insertTask(title: String): Long {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return -1L
        val task = TaskEntity(
            title = trimmed,
            isCompleted = false,
            createdAt = System.currentTimeMillis()
        )
        return taskDao.insertTask(task)
    }

    suspend fun insertTaskDirect(task: TaskEntity): Long {
        return taskDao.insertTask(task)
    }

    suspend fun toggleCompleted(task: TaskEntity) {
        val updated = task.copy(
            isCompleted = !task.isCompleted,
            completedAt = if (!task.isCompleted) System.currentTimeMillis() else null
        )
        taskDao.updateTask(updated)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(id: Long) {
        taskDao.deleteTaskById(id)
    }

    suspend fun clearCompleted(): Int {
        return taskDao.deleteCompletedTasks()
    }
}
