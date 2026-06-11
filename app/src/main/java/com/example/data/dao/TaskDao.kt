package com.example.data.dao

import androidx.room.*
import com.example.data.model.TaskItem
import com.example.data.model.TaskList
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // --- Task Lists ---
    @Query("SELECT * FROM task_lists ORDER BY id ASC")
    fun getAllLists(): Flow<List<TaskList>>

    @Query("SELECT COUNT(*) FROM task_lists")
    suspend fun getListsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: TaskList): Long

    @Delete
    suspend fun deleteList(list: TaskList)

    @Query("DELETE FROM task_items WHERE listId = :listId")
    suspend fun deleteTasksByListId(listId: Int)

    @Transaction
    suspend fun deleteListWithTasks(list: TaskList) {
        deleteTasksByListId(list.id)
        deleteList(list)
    }

    // --- Task Items ---
    @Query("SELECT * FROM task_items ORDER BY timestamp DESC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM task_items WHERE listId = :listId ORDER BY timestamp DESC")
    fun getTasksByList(listId: Int): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(item: TaskItem): Long

    @Update
    suspend fun updateTask(item: TaskItem)

    @Delete
    suspend fun deleteTask(item: TaskItem)

    @Query("UPDATE task_items SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Int, isCompleted: Boolean)
}
