package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Advertiser
import com.example.data.model.TaskItem
import com.example.data.model.TaskList
import com.example.data.repository.TaskRepository
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

        // Seed data on launch if lists and advertisers are empty
        seedDataIfNecessary()

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

    // --- Seeds default data if database tables are empty ---
    private fun seedDataIfNecessary() {
        viewModelScope.launch {
            // Check if lists are empty
            taskLists.first { it.isNotEmpty() || taskLists.value.isEmpty() }
            if (taskLists.value.isEmpty()) {
                val listId1 = repository.insertList(TaskList(name = "Trabalho 💼", colorHex = "#3F51B5", iconName = "work")).toInt()
                val listId2 = repository.insertList(TaskList(name = "Pessoal 🏠", colorHex = "#4CAF50", iconName = "home")).toInt()
                val listId3 = repository.insertList(TaskList(name = "Estudos 📚", colorHex = "#FF9800", iconName = "book")).toInt()
                val listId4 = repository.insertList(TaskList(name = "Compras 🛒", colorHex = "#9C27B0", iconName = "shopping")).toInt()

                // Insert some default tasks
                repository.insertTask(TaskItem(listId = listId1, title = "Revisar relatório de vendas", description = "Analisar dados do Q2 e repassar para gerência", priority = "HIGH"))
                repository.insertTask(TaskItem(listId = listId1, title = "Agendar reunião de equipe", description = "Google Meet às 14h sobre novas metas", priority = "MEDIUM"))
                
                repository.insertTask(TaskItem(listId = listId2, title = "Comprar mantimentos", description = "Leite, ovos, pão de queijo e frutas", priority = "LOW"))
                repository.insertTask(TaskItem(listId = listId2, title = "Exercício de 40 min", description = "Treino aeróbico ou corrida no parque", priority = "MEDIUM", isCompleted = true))

                repository.insertTask(TaskItem(listId = listId3, title = "Módulo 2 de Kotlin", description = "Entender StateFlow e corrotinas do Jetpack Compose", priority = "HIGH"))
                repository.insertTask(TaskItem(listId = listId3, title = "Ler 10 páginas de livro", description = "Leitura diária focado em desenvolvimento de carreira", priority = "LOW"))

                repository.insertTask(TaskItem(listId = listId4, title = "Comprar presente de aniversário", description = "Comprar livro ou perfume para o pai", priority = "MEDIUM"))
            }

            // Check if advertisers are empty
            advertisers.first { it.isNotEmpty() || advertisers.value.isEmpty() }
            if (advertisers.value.isEmpty()) {
                repository.insertAdvertiser(
                    Advertiser(
                        name = "Cafeteria Santo Grão ☕",
                        slogan = "Melhores grãos e espressos artesanais!",
                        bannerText = "Ganhe 15% de desconto apresentando este anúncio com o cupom CAFE15. Visite nossa cafeteria no centro!",
                        targetUrl = "https://santograo.com.br/promo",
                        colorHex = "#8D6E63", // Brown
                        iconName = "coffee",
                        clickCount = 14
                    )
                )
                repository.insertAdvertiser(
                    Advertiser(
                        name = "Academia MoveFit 💪",
                        slogan = "Matrícula grátis + Brinde exclusivo!",
                        bannerText = "Seu projeto de vida saudável começa agora. Use o cupom MOVEFIT para treinar de graça a primeira semana inteira!",
                        targetUrl = "https://movefitacademy.com/aistudio",
                        colorHex = "#2E7D32", // Green
                        iconName = "fitness",
                        clickCount = 8
                    )
                )
                repository.insertAdvertiser(
                    Advertiser(
                        name = "Delivery Pizza Premium 🍕",
                        slogan = "Peça uma pizza grande e ganhe uma broto doce",
                        bannerText = "Tradicional massa napolitana assada no forno a lenha. Digite PIZZALOVER no checkout para garantir o brinde especial!",
                        targetUrl = "https://premium.pizzaria.br/cupom",
                        colorHex = "#E64A19", // Deep Orange
                        iconName = "fastfood",
                        clickCount = 23
                    )
                )
                repository.insertAdvertiser(
                    Advertiser(
                        name = "DevCourses Tech 💻",
                        slogan = "Aprenda Android & Kotlin com especialistas",
                        bannerText = "Acelere sua carreira tech hoje de forma descomplicada. Matricule-se com cupom DEVKT20 para 20% off em toda a plataforma!",
                        targetUrl = "https://devcourses.com/pro-kotlin",
                        colorHex = "#0288D1", // Blue color
                        iconName = "computer",
                        clickCount = 37
                    )
                )
            }
        }
    }

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
