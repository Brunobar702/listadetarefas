package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Advertiser
import com.example.data.model.TaskItem
import com.example.data.model.TaskList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TaskViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Observe state from ViewModel
    val lists by viewModel.taskLists.collectAsStateWithLifecycle()
    val filteredTasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val selectedListId by viewModel.selectedListId.collectAsStateWithLifecycle()
    val currentFilter by viewModel.taskFilter.collectAsStateWithLifecycle()
    val footerAd by viewModel.currentFooterAdvertiser.collectAsStateWithLifecycle()
    val advertiserDetail by viewModel.selectedAdvertiserForDetail.collectAsStateWithLifecycle()
    val allAdvertisers by viewModel.advertisers.collectAsStateWithLifecycle()

    // Dialog / Sheet visibility states
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddListDialog by remember { mutableStateOf(false) }
    var showPartnerPanelDialog by remember { mutableStateOf(false) }
    var showCreatePartnerDialog by remember { mutableStateOf(false) }

    // Helpers to get specific lists names
    val selectedListName = remember(selectedListId, lists) {
        if (selectedListId == -1) "Todas as Listas"
        else lists.find { it.id == selectedListId }?.name ?: "Lista"
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        topBar = {
            LargeTopAppBar(
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 8.dp)
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .clickable {
                                Toast.makeText(context, "Sleek Interface Active", Toast.LENGTH_SHORT).show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.5.dp),
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.width(18.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(MaterialTheme.colorScheme.onSurfaceVariant))
                            Box(modifier = Modifier.fillMaxWidth(0.6f).height(2.dp).background(MaterialTheme.colorScheme.onSurfaceVariant))
                            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(MaterialTheme.colorScheme.onSurfaceVariant))
                        }
                    }
                },
                title = {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Text(
                            text = "Minhas Listas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.5).sp
                        )
                        val pendingCount = allTasks.count { !it.isCompleted }
                        Text(
                            text = "Você tem $pendingCount tarefas para hoje",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 16.dp, top = 8.dp)
                    ) {
                        // Advertisers Panel Access Button styled sleekly
                        IconButton(
                            onClick = { showPartnerPanelDialog = true },
                            modifier = Modifier
                                .testTag("advertisers_panel_button")
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Gerenciar Anunciantes",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // MD Avatar from mockup theme
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "MD",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Elegant Sleek Interactive Rotating Footer
            FooterAdBanner(
                advertiser = footerAd,
                activeAdvertisersCount = allAdvertisers.size,
                totalTasksCount = allTasks.size,
                totalClickCount = allAdvertisers.sumOf { it.clickCount },
                onAdClick = { ad ->
                    viewModel.clickAdvertiser(ad.id)
                },
                onNextAdClick = {
                    viewModel.rotateAdvertiserNext()
                },
                onManageClick = {
                    showPartnerPanelDialog = true
                }
            )
        },
        floatingActionButton = {
            // Floating button to add task (only if lists aren't empty)
            if (lists.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    text = { Text("Nova Tarefa") },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Nova Tarefa") },
                    onClick = { showAddTaskDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .testTag("add_task_fab")
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // SECTION 1: Horizontal Lists Category Selector
            Text(
                text = "Listas de Tarefas",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Todos" standard general filter
                item {
                    val isSelected = selectedListId == -1
                    FilterChipComponent(
                        name = "Todas 🌟",
                        isSelected = isSelected,
                        colorHex = "#673AB7",
                        onClick = { viewModel.selectList(-1) },
                        onDeleteClick = null
                    )
                }

                // Dynamic Categories loaded from DB
                items(lists) { list ->
                    val isSelected = selectedListId == list.id
                    FilterChipComponent(
                        name = list.name,
                        isSelected = isSelected,
                        colorHex = list.colorHex,
                        onClick = { viewModel.selectList(list.id) },
                        onDeleteClick = {
                            viewModel.deleteList(list)
                            Toast.makeText(context, "Lista '${list.name}' removida", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Plus button to insert a brand new category
                item {
                    OutlinedButton(
                        onClick = { showAddListDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("add_list_chip")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Criar Lista",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Nova Lista", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2: Filter Tabs (Todas, Pendentes, Concluídas)
            TabRow(
                selectedTabIndex = currentFilter.ordinal,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                TaskFilter.values().forEach { filter ->
                    Tab(
                        selected = currentFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        text = {
                            Text(
                                text = when (filter) {
                                    TaskFilter.ALL -> "Todas (${filteredTasks.size})"
                                    TaskFilter.PENDING -> "Pendentes (${filteredTasks.count { !it.isCompleted }})"
                                    TaskFilter.COMPLETED -> "Concluídas (${filteredTasks.count { it.isCompleted }})"
                                },
                                fontWeight = if (currentFilter == filter) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.testTag("filter_tab_${filter.name.lowercase()}")
                    )
                }
            }

            // SECTION 3: Vertical Task List
            if (filteredTasks.isEmpty()) {
                EmptyStateView(
                    filter = currentFilter,
                    selectedListName = selectedListName,
                    hasLists = lists.isNotEmpty(),
                    onAddFirstListClick = { showAddListDialog = true },
                    onAddTaskClick = { showAddTaskDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("tasks_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        // Dynamically look up the category list attributes associated with this task
                        val listAssociation = lists.find { it.id == task.listId }

                        TaskItemRow(
                            task = task,
                            listName = listAssociation?.name ?: "Geral",
                            listColorHex = listAssociation?.colorHex ?: "#607D8B",
                            onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                            onDeleteTask = {
                                viewModel.deleteTask(task)
                                Toast.makeText(context, "Tarefa removida", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGS AND MODAL BOTTOM SHEETS ---

    // 1. ADD TASK DIALOG
    if (showAddTaskDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var taskDesc by remember { mutableStateOf("") }
        var selectedCategoryIndex by remember { mutableStateOf(0) }
        var selectedPriority by remember { mutableStateOf("MEDIUM") }

        // Find initial list
        val defaultListId = remember(lists, selectedListId) {
            val idx = lists.indexOfFirst { it.id == selectedListId }
            if (idx != -1) idx else 0
        }
        LaunchedEffect(defaultListId) {
            selectedCategoryIndex = defaultListId
        }

        // Dropdown expansion state
        var priorityExpanded by remember { mutableStateOf(false) }
        var categoryExpanded by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showAddTaskDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Nova Tarefa 📝",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Title
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Título da Tarefa") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_task_title")
                    )

                    // Description
                    OutlinedTextField(
                        value = taskDesc,
                        onValueChange = { taskDesc = it },
                        label = { Text("Breve Descrição") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category Selector Dropdown
                    if (lists.isNotEmpty()) {
                        Box {
                            OutlinedTextField(
                                value = lists[selectedCategoryIndex].name,
                                onValueChange = {},
                                label = { Text("Lista / Categoria") },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { categoryExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir")
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                lists.forEachIndexed { idx, taskList ->
                                    DropdownMenuItem(
                                        text = { Text(taskList.name) },
                                        onClick = {
                                            selectedCategoryIndex = idx
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Priority Selector
                    Box {
                        OutlinedTextField(
                            value = when (selectedPriority) {
                                "HIGH" -> "🚨 Alta Prioridade"
                                "MEDIUM" -> "⚡ Média Prioridade"
                                else -> "🟢 Baixa Prioridade"
                            },
                            onValueChange = {},
                            label = { Text("Prioridade") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { priorityExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Expandir Prioridade")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = priorityExpanded,
                            onDismissRequest = { priorityExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("🚨 Alta Prioridade") },
                                onClick = { selectedPriority = "HIGH"; priorityExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("⚡ Média Prioridade") },
                                onClick = { selectedPriority = "MEDIUM"; priorityExpanded = false }
                            )
                            DropdownMenuItem(
                                text = { Text("🟢 Baixa Prioridade") },
                                onClick = { selectedPriority = "LOW"; priorityExpanded = false }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddTaskDialog = false }) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (taskTitle.trim().isNotEmpty() && lists.isNotEmpty()) {
                                    val destinationListId = lists[selectedCategoryIndex].id
                                    viewModel.addTask(
                                        listId = destinationListId,
                                        title = taskTitle.trim(),
                                        description = taskDesc.trim(),
                                        priority = selectedPriority
                                    )
                                    showAddTaskDialog = false
                                } else {
                                    Toast.makeText(context, "Insira um título válido!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("save_task_button")
                        ) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }
    }

    // 2. ADD LIST DIALOG
    if (showAddListDialog) {
        var listName by remember { mutableStateOf("") }
        var selectedColorHex by remember { mutableStateOf("#4CAF50") } // Green default
        val colorOptions = listOf("#4CAF50", "#2196F3", "#9C27B0", "#FF9800", "#E91E63", "#00BCD4", "#E64A19", "#673AB7")

        Dialog(onDismissRequest = { showAddListDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Nova Lista / Categoria 📁",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = listName,
                        onValueChange = { listName = it },
                        label = { Text("Nome da Lista (ex: Mercado 🛒)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_list_name")
                    )

                    Text(
                        text = "Escolha um tom de destaque:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    // Color palette Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        colorOptions.forEach { colorStr ->
                            val colorValue = Color(android.graphics.Color.parseColor(colorStr))
                            val isSelected = selectedColorHex == colorStr

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colorValue)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = colorStr }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddListDialog = false }) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (listName.trim().isNotEmpty()) {
                                    viewModel.addList(
                                        name = listName.trim(),
                                        colorHex = selectedColorHex,
                                        iconName = "list"
                                    )
                                    showAddListDialog = false
                                } else {
                                    Toast.makeText(context, "Insira um nome válido!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("save_list_button")
                        ) {
                            Text("Criar")
                        }
                    }
                }
            }
        }
    }

    // 3. ADVERTISER MANAGEMENT CENTER PANEL DIALOG
    if (showPartnerPanelDialog) {
        Dialog(onDismissRequest = { showPartnerPanelDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Painel de Anunciantes 📣",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { showPartnerPanelDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }

                    Text(
                        text = "Gerencie os patrocinadores do rodapé do app, veja estatísticas de cliques simuladas e crie anúncios.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showCreatePartnerDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_advertiser_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Criar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cadastrar Novo Anunciante", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Anunciantes no Sistema (${allAdvertisers.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (allAdvertisers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Nenhum anunciante cadastrado.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allAdvertisers) { ad ->
                                val adColor = Color(android.graphics.Color.parseColor(ad.colorHex))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(adColor)
                                                )
                                                Text(
                                                    text = ad.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Active Toggle Button
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = if (ad.isActive) "Ativo" else "Inativo",
                                                    fontSize = 11.sp,
                                                    color = if (ad.isActive) Color.Green else Color.Gray,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Switch(
                                                    checked = ad.isActive,
                                                    onCheckedChange = { isChecked ->
                                                        viewModel.updateAdvertiserItem(ad.copy(isActive = isChecked))
                                                    },
                                                    modifier = Modifier.scale(0.7f)
                                                )
                                            }
                                        }

                                        Text(
                                            text = ad.slogan,
                                            fontSize = 12.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Cliques: 📊 ${ad.clickCount}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        // Toggle click simulated instantly
                                                        viewModel.clickAdvertiser(ad.id)
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.PlayArrow,
                                                        contentDescription = "Testar",
                                                        tint = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        viewModel.deleteAdvertiser(ad)
                                                        Toast.makeText(context, "Anunciante removido", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Excluir Anunciante",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 4. CREATE NEW ADVERTISER DIALOG
    if (showCreatePartnerDialog) {
        var name by remember { mutableStateOf("") }
        var slogan by remember { mutableStateOf("") }
        var bannerText by remember { mutableStateOf("") }
        var targetUrl by remember { mutableStateOf("") }
        var bannerColorHex by remember { mutableStateOf("#FF9800") } // Amber Default
        var selectedIconType by remember { mutableStateOf("coffee") }

        val adColorChoices = listOf("#FF9800", "#00BCD4", "#0288D1", "#E64A19", "#8D6E63", "#2E7D32", "#673AB7", "#D32F2F")
        val iconTypeChoices = listOf("coffee", "fitness", "fastfood", "computer", "store", "star")

        Dialog(onDismissRequest = { showCreatePartnerDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Cadastrar Patrocinador 💡",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome da Empresa (ex: Pizzaria Italia)") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_advertiser_name")
                    )

                    OutlinedTextField(
                        value = slogan,
                        onValueChange = { slogan = it },
                        label = { Text("Mensagem Curta / Slogan") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bannerText,
                        onValueChange = { bannerText = it },
                        label = { Text("Texto da Promoção & Cupom") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = targetUrl,
                        onValueChange = { targetUrl = it },
                        label = { Text("URL de Redirecionamento") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Destaque Visual da Campanha:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Color row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        adColorChoices.forEach { chosenHex ->
                            val parsed = Color(android.graphics.Color.parseColor(chosenHex))
                            val isChosen = bannerColorHex == chosenHex
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(parsed)
                                    .border(
                                        width = if (isChosen) 2.dp else 0.dp,
                                        color = if (isChosen) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { bannerColorHex = chosenHex }
                            )
                        }
                    }

                    Text(
                        text = "Ícone Temático da Campanha:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Icons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        iconTypeChoices.forEach { iconName ->
                            val isSelected = selectedIconType == iconName
                            val iconVector = when (iconName) {
                                "coffee" -> Icons.Default.Favorite
                                "fitness" -> Icons.Default.Star
                                "fastfood" -> Icons.Default.Home
                                "computer" -> Icons.Default.Settings
                                "store" -> Icons.Default.List
                                else -> Icons.Default.Check
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedIconType = iconName },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = iconName,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCreatePartnerDialog = false }) {
                            Text("Voltar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.trim().isNotEmpty() && slogan.trim().isNotEmpty()) {
                                    viewModel.addAdvertiser(
                                        name = name.trim(),
                                        slogan = slogan.trim(),
                                        bannerText = bannerText.trim(),
                                        targetUrl = if (targetUrl.trim().isEmpty()) "https://ai.studio" else targetUrl.trim(),
                                        colorHex = bannerColorHex,
                                        iconName = selectedIconType
                                    )
                                    showCreatePartnerDialog = false
                                } else {
                                    Toast.makeText(context, "Insira os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("save_advertiser_button")
                        ) {
                            Text("Salvar Anunciante")
                        }
                    }
                }
            }
        }
    }

    // 5. ADVERTISER ACTIVE PROMO DETAILS DIALOG (Simulating clicking the ad footer)
    advertiserDetail?.let { activeAd ->
        val bgBrush = Color(android.graphics.Color.parseColor(activeAd.colorHex))
        Dialog(onDismissRequest = { viewModel.clearSelectedAdvertiser() }) {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Visual Header with gradient background of candidate hex
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(bgBrush, bgBrush.copy(alpha = 0.7f))
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.22f), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Patrocinado",
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ANÚNCIO PATROCINADO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = activeAd.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Text(
                                text = activeAd.slogan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = activeAd.bannerText,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )

                        // Coupon Code display box
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = bgBrush.copy(alpha = 0.12f)),
                            border = BorderStroke(1.5.dp, bgBrush.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Código do Cupom:",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = if (activeAd.bannerText.contains("CAFE15")) "CAFE15"
                                        else if (activeAd.bannerText.contains("MOVEFIT")) "MOVEFIT"
                                        else if (activeAd.bannerText.contains("PIZZALOVER")) "PIZZALOVER"
                                        else if (activeAd.bannerText.contains("DEVKT20")) "DEVKT20"
                                        else "STUDIOADS",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = bgBrush
                                    )
                                }

                                Button(
                                    onClick = {
                                        val promoToken = if (activeAd.bannerText.contains("CAFE15")) "CAFE15"
                                        else if (activeAd.bannerText.contains("MOVEFIT")) "MOVEFIT"
                                        else if (activeAd.bannerText.contains("PIZZALOVER")) "PIZZALOVER"
                                        else if (activeAd.bannerText.contains("DEVKT20")) "DEVKT20"
                                        else "STUDIOADS"

                                        clipboardManager.setText(AnnotatedString(promoToken))
                                        Toast.makeText(context, "Cupom copiado para a área de transferência! 🎉", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = bgBrush),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("copy_coupon_button")
                                ) {
                                    Text("Copiar", color = Color.White)
                                }
                            }
                        }

                        // Web destination disclaimer
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Toast.makeText(context, "Redirecionando para: ${activeAd.targetUrl} 🚀", Toast.LENGTH_SHORT).show()
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Website",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Acesse: ${activeAd.targetUrl}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textDecoration = TextDecoration.Underline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = { viewModel.clearSelectedAdvertiser() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Fechar")
                        }
                    }
                }
            }
        }
    }
}

// --- SUB LEVEL HELPER COMPOSABLES ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilterChipComponent(
    name: String,
    isSelected: Boolean,
    colorHex: String,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)?
) {
    val colorValue = remember(colorHex) {
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color(0xFF0061A4)
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    if (onDeleteClick != null) {
                        showDeleteDialog = true
                    }
                }
            ),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) colorValue else colorValue.copy(alpha = 0.12f),
        border = null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else colorValue)
            )
            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else colorValue
            )
        }
    }

    if (showDeleteDialog && onDeleteClick != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Categoria") },
            text = { Text("Tem certeza que deseja excluir a lista '$name' e todas as tarefas vinculadas a ela de forma definitiva?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun TaskItemRow(
    task: TaskItem,
    listName: String,
    listColorHex: String,
    onToggleCompletion: () -> Unit,
    onDeleteTask: () -> Unit
) {
    val listColor = remember(listColorHex) {
        try {
            Color(android.graphics.Color.parseColor(listColorHex))
        } catch (e: Exception) {
            Color(0xFF0061A4)
        }
    }

    val isCompleted = task.isCompleted

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleCompletion() }
            .testTag("task_item_${task.id}")
            .graphicsLayer {
                alpha = if (isCompleted) 0.6f else 1f
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompleted) 0.dp else 0.5.dp
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompleted) Color.Transparent else Color(0xFFF1F5F9)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(0.85f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tailwind Mockup Checkbox (Custom Square Shape with rounded corners)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isCompleted) listColor else Color.Transparent
                        )
                        .border(
                            width = 2.dp,
                            color = if (isCompleted) Color.Transparent else Color(0xFFCBD5E1),
                            shape = RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Concluído",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Priority Badge indicator
                        val priorityLabel = when (task.priority) {
                            "HIGH" -> "Alta"
                            "MEDIUM" -> "Média"
                            else -> "Baixa"
                        }
                        val priorityColor = when (task.priority) {
                            "HIGH" -> Color(0xFFD32F2F)
                            "MEDIUM" -> Color(0xFFFFA000)
                            else -> Color(0xFF388E3C)
                        }

                        Text(
                            text = priorityLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = priorityColor,
                            modifier = Modifier
                                .background(priorityColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )

                        // List name badge
                        Text(
                            text = listName,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = listColor,
                            modifier = Modifier
                                .background(listColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = task.title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else Color(0xFF1E293B), // slate-800
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (task.description.isNotEmpty()) {
                        Text(
                            text = task.description,
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8), // slate-400
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            IconButton(
                onClick = onDeleteTask,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Excluir Tarefa",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun FooterAdBanner(
    advertiser: Advertiser?,
    activeAdvertisersCount: Int,
    totalTasksCount: Int,
    totalClickCount: Int,
    onAdClick: (Advertiser) -> Unit,
    onNextAdClick: () -> Unit,
    onManageClick: () -> Unit
) {
    if (advertiser == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF131416))
                .padding(20.dp)
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Text("Carregando parcerias...", fontSize = 13.sp, color = Color(0xFF94A3B8))
        }
        return
    }

    val adColor = remember(advertiser.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(advertiser.colorHex))
        } catch (e: Exception) {
            Color(0xFF0061A4)
        }
    }

    Card(
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("footer_advertiser_card"),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ANÚNCIO PATROCINADO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = advertiser.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                        .clickable { onNextAdClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Próximo anúncio",
                        tint = Color(0xFFE2E8F0),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Girar Parceiro",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }

            Text(
                text = "${advertiser.slogan} • ${advertiser.bannerText}",
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(text = "Tarefas", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "$totalTasksCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(text = "Parceiros", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "0$activeAdvertisersCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(text = "Cliques", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$totalClickCount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onAdClick(advertiser) },
                    colors = ButtonDefaults.buttonColors(containerColor = adColor),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(44.dp)
                        .testTag("ad_ver_mais_button")
                ) {
                    Text(
                        text = "Ver Patrocinador ✨",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onManageClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .testTag("ad_manage_panel_button")
                ) {
                    Text(
                        text = "Parcerias",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    filter: TaskFilter,
    selectedListName: String,
    hasLists: Boolean,
    onAddFirstListClick: () -> Unit,
    onAddTaskClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Vazio",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier.size(72.dp)
            )

            Text(
                text = if (!hasLists) "Nenhuma Lista Criada 📂"
                else when (filter) {
                    TaskFilter.ALL -> "Tudo em dia! 🎉"
                    TaskFilter.PENDING -> "Nenhuma tarefa pendente!"
                    TaskFilter.COMPLETED -> "Nenhuma tarefa concluída ainda."
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (!hasLists) "Crie sua primeira lista de tarefas para começar a organizar sua rotina agora mesmo!"
                else "Nenhuma tarefa foi encontrada correspondente em '$selectedListName'. Clique abaixo para adicionar ou sinta-se orgulhoso da sua produtividade!",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 280.dp)
            )

            if (!hasLists) {
                Button(
                    onClick = onAddFirstListClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Criar Minha Primeira Lista")
                }
            } else if (filter != TaskFilter.COMPLETED) {
                Button(
                    onClick = onAddTaskClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("empty_state_add_task_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Adicionar Tarefa")
                }
            }
        }
    }
}
