package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_lists")
data class TaskList(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String, // Hex color for category branding (e.g. #FF5733)
    val iconName: String // Icon representation code (e.g. "shopping", "work", "personal")
)
