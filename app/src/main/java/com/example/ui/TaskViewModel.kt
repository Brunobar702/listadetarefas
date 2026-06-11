package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Advertiser
import com.example.data.model.TaskItem
import com.example.data.model.TaskList
import com.example.data.repository.TaskRepository
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TaskFilter {
    ALL, PENDING, COMPLETED
}

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository

    // --- Core Flows ---
    val taskLists: StateFlow<List<TaskList>>
    val allTasks: StateFlow<List<TaskItem>>
    val advertisers: StateFlow<List<Advertiser>>
    val activeAdvertisers: StateFlow<List<Advertiser>>

    // --- UI Selection States ---
    private val _selectedListId = MutableStateFlow<Int>(-1) // -1 means "Todas" (All Lists)
    val selectedListId = _selectedListId.asStateFlow()

    private val _taskFilter = MutableStateFlow(TaskFilter.ALL)
    val taskFilter = _taskFilter.asStateFlow()

    // --- Current Active Footer Advertiser rotation index ---
    private val _currentAdvertiserIndex = MutableStateFlow(0)
    val currentAdvertiserIndex = _currentAdvertiserIndex.asStateFlow()

    // --- Detail state for clicked advertiser ---
    private val _selectedAdvertiserForDetail = MutableStateFlow<Advertiser?>(null)
    val selectedAdvertiserForDetail = _selectedAdvertiserForDetail.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = TaskRepository(db.taskDao(), db.advertiserDao())

        taskLists = repository.allLists
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allTasks = repository.allTasks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        advertisers = repository.allAdvertisers
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        activeAdvertisers = repository.activeAdvertisers
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Clear database once globally to start off freshly zeroed out (empty), as requested
        val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstClear = prefs.getBoolean("database_cleared_once_v4", false)
        if (!isFirstClear) {
            viewModelScope.launch(Dispatchers.IO) {
                db.clearAllTables()
                prefs.edit().putBoolean("database_cleared_once_v4", true).apply()
            }
        }

        // Auto-increment advertiser rotation index for footer every 8 seconds if there are multiple active ads
        startAdvertiserRotation()
    }

    // --- Combined Reactive Filtered Tasks ---
    val filteredTasks: StateFlow<List<TaskItem>> = combine(
        allTasks,
        _selectedListId,
        _taskFilter
    ) { tasks, listId, filter ->
        var list = if (listId == -1) {
            tasks
        } else {
            tasks.filter { it.listId == listId }
        }

        list = when (filter) {
            TaskFilter.ALL -> list
            TaskFilter.PENDING -> list.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> list.filter { it.isCompleted }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Current footer advertiser based on index and active ads list ---
    val currentFooterAdvertiser: StateFlow<Advertiser?> = combine(
        activeAdvertisers,
        _currentAdvertiserIndex
    ) { activeAds, index ->
        if (activeAds.isEmpty()) null
        else {
            val safeIndex = index % activeAds.size
            activeAds[safeIndex]
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)



    private fun startAdvertiserRotation() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(8000)
                val adsCount = activeAdvertisers.value.size
                if (adsCount > 1) {
                    _currentAdvertiserIndex.update { (it + 1) % adsCount }
                }
            }
        }
    }

    // --- Advertiser Selection rotation handle ---
    fun rotateAdvertiserNext() {
        val adsCount = activeAdvertisers.value.size
        if (adsCount > 1) {
            _currentAdvertiserIndex.update { (it + 1) % adsCount }
        }
    }

    fun selectList(id: Int) {
        _selectedListId.value = id
    }

    fun setFilter(filter: TaskFilter) {
        _taskFilter.value = filter
    }

    // --- Task Actions ---
    fun addTask(listId: Int, title: String, description: String, priority: String) {
        viewModelScope.launch {
            repository.insertTask(
                TaskItem(
                    listId = listId,
                    title = title,
                    description = description,
                    priority = priority
                )
            )
        }
    }

    fun toggleTaskCompletion(task: TaskItem) {
        viewModelScope.launch {
            repository.updateTaskCompletion(task.id, !task.isCompleted)
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- List Actions ---
    fun addList(name: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            val newListId = repository.insertList(
                TaskList(
                    name = name,
                    colorHex = colorHex,
                    iconName = iconName
                )
            )
            // Auto select the newly created list to immediately view/add tasks to it
            _selectedListId.value = newListId.toInt()
        }
    }

    fun deleteList(list: TaskList) {
        viewModelScope.launch {
            repository.deleteList(list)
            if (_selectedListId.value == list.id) {
                _selectedListId.value = -1 // Reset selection to "Todas"
            }
        }
    }

    // --- Advertiser Actions ---
    fun addAdvertiser(name: String, slogan: String, bannerText: String, targetUrl: String, colorHex: String, iconName: String) {
        viewModelScope.launch {
            repository.insertAdvertiser(
                Advertiser(
                    name = name,
                    slogan = slogan,
                    bannerText = bannerText,
                    targetUrl = targetUrl,
                    colorHex = colorHex,
                    iconName = iconName
                )
            )
        }
    }

    fun updateAdvertiserItem(advertiser: Advertiser) {
        viewModelScope.launch {
            repository.updateAdvertiser(advertiser)
        }
    }

    fun deleteAdvertiser(advertiser: Advertiser) {
        viewModelScope.launch {
            repository.deleteAdvertiser(advertiser)
        }
    }

    fun clickAdvertiser(id: Int) {
        viewModelScope.launch {
            repository.incrementAdvertiserClick(id)
            // Also find advertiser object to trigger detail view
            val advertiser = activeAdvertisers.value.find { it.id == id }
                ?: advertisers.value.find { it.id == id }
            if (advertiser != null) {
                // Return updated object with incremented click for UI instantly
                _selectedAdvertiserForDetail.value = advertiser.copy(clickCount = advertiser.clickCount + 1)
            }
        }
    }

    fun clearSelectedAdvertiser() {
        _selectedAdvertiserForDetail.value = null
    }
}
