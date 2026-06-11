package com.example.data.repository

import com.example.data.dao.AdvertiserDao
import com.example.data.dao.TaskDao
import com.example.data.model.Advertiser
import com.example.data.model.TaskItem
import com.example.data.model.TaskList
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val advertiserDao: AdvertiserDao
) {
    suspend fun getListsCount(): Int = taskDao.getListsCount()
    suspend fun getAdvertisersCount(): Int = advertiserDao.getAdvertisersCount()

    // --- Task Lists ---
    val allLists: Flow<List<TaskList>> = taskDao.getAllLists()

    suspend fun insertList(list: TaskList): Long {
        return taskDao.insertList(list)
    }

    suspend fun deleteList(list: TaskList) {
        taskDao.deleteListWithTasks(list)
    }

    // --- Task Items ---
    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()

    fun getTasksByList(listId: Int): Flow<List<TaskItem>> {
        return taskDao.getTasksByList(listId)
    }

    suspend fun insertTask(item: TaskItem): Long {
        return taskDao.insertTask(item)
    }

    suspend fun updateTask(item: TaskItem) {
        taskDao.updateTask(item)
    }

    suspend fun deleteTask(item: TaskItem) {
        taskDao.deleteTask(item)
    }

    suspend fun updateTaskCompletion(taskId: Int, isCompleted: Boolean) {
        taskDao.updateTaskCompletion(taskId, isCompleted)
    }

    // --- Advertisers ---
    val allAdvertisers: Flow<List<Advertiser>> = advertiserDao.getAllAdvertisers()
    val activeAdvertisers: Flow<List<Advertiser>> = advertiserDao.getActiveAdvertisers()

    suspend fun insertAdvertiser(advertiser: Advertiser): Long {
        return advertiserDao.insertAdvertiser(advertiser)
    }

    suspend fun updateAdvertiser(advertiser: Advertiser) {
        advertiserDao.updateAdvertiser(advertiser)
    }

    suspend fun deleteAdvertiser(advertiser: Advertiser) {
        advertiserDao.deleteAdvertiser(advertiser)
    }

    suspend fun incrementAdvertiserClick(id: Int) {
        advertiserDao.incrementClickCount(id)
    }
}
