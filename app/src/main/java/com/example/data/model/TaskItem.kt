package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_items")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listId: Int, // Foreign key reference to TaskList (logical connection)
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val timestamp: Long = System.currentTimeMillis()
)
