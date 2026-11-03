package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa uma tarefa guardada na base de dados local Room do dispositivo.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
