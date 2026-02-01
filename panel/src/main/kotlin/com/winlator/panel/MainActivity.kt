package com.winlator.panel

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.winlator.panel.data.*
import com.winlator.panel.ui.theme.WinlatorPanelTheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("admin_prefs", MODE_PRIVATE)
        val savedToken = prefs.getString("auth_token", null)
        if (savedToken != null) {
            SupabaseClient.authToken = savedToken
        }

        setContent {
            WinlatorPanelTheme {
                var isLoggedIn by remember { mutableStateOf(savedToken != null) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isLoggedIn) {
                        LoginScreen(onLoginSuccess = { token ->
                            prefs.edit().putString("auth_token", token).apply()
                            isLoggedIn = true
                        })
                    } else {
                        PanelMainScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val service = remember {
        Retrofit.Builder()
            .baseUrl(SupabaseClient.URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseService::class.java)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Admin Panel Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        val resp = service.login(SupabaseClient.API_KEY, LoginRequest(email, password))
                        SupabaseClient.authToken = resp.accessToken
                        onLoginSuccess(resp.accessToken)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erro no login: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            else Text("Login")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelMainScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("admin_prefs", android.content.Context.MODE_PRIVATE) }

    var categories by remember { mutableStateOf<List<SupabaseCategory>>(emptyList()) }
    var repositories by remember { mutableStateOf<List<SupabaseRepo>>(emptyList()) }
    var appConfig by remember { mutableStateOf<AppConfig?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var tabIndex by remember { mutableIntStateOf(0) }

    val service = remember {
        Retrofit.Builder()
            .baseUrl(SupabaseClient.URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseService::class.java)
    }

    val mgmtService = remember {
        Retrofit.Builder()
            .baseUrl("https://api.supabase.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseManagementService::class.java)
    }

    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                categories = service.getCategories(SupabaseClient.API_KEY, SupabaseClient.authHeader)
                repositories = service.getRepositories(SupabaseClient.API_KEY, SupabaseClient.authHeader)
                appConfig = service.getAppConfig(SupabaseClient.API_KEY, SupabaseClient.authHeader).firstOrNull()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Winlator Hub Panel") })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, null) },
                    label = { Text("Dashboard") },
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Category, null) },
                    label = { Text("Categorias") },
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, null) },
                    label = { Text("Repos") },
                    selected = tabIndex == 2,
                    onClick = { tabIndex = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.VideogameAsset, null) },
                    label = { Text("Jogos") },
                    selected = tabIndex == 3,
                    onClick = { tabIndex = 3 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Config") },
                    selected = tabIndex == 4,
                    onClick = { tabIndex = 4 }
                )
            }
        },
        floatingActionButton = {
            if (tabIndex == 1 || tabIndex == 2) {
                var showAddDialog by remember { mutableStateOf(false) }
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, null)
                }
                if (showAddDialog) {
                    if (tabIndex == 1) {
                        AddCategoryDialog(
                            onDismiss = { showAddDialog = false },
                            onConfirm = { name ->
                                scope.launch {
                                    try {
                                        service.createCategory(SupabaseClient.API_KEY, SupabaseClient.authHeader, SupabaseCategory(name = name))
                                        loadData()
                                        showAddDialog = false
                                    } catch (e: Exception) { e.printStackTrace() }
                                }
                            }
                        )
                    } else {
                        AddRepoDialog(
                            categories = categories,
                            onDismiss = { showAddDialog = false },
                            onConfirm = { name, owner, repo, desc, catId ->
                                scope.launch {
                                    try {
                                        service.createRepository(
                                            SupabaseClient.API_KEY, SupabaseClient.authHeader,
                                            SupabaseRepo(name = name, owner = owner, repo = repo, description = desc, categoryId = catId)
                                        )
                                        loadData()
                                        showAddDialog = false
                                    } catch (e: Exception) { e.printStackTrace() }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(Modifier.padding(padding)) {
                when (tabIndex) {
                    0 -> DashboardScreen(mgmtService, prefs)
                    1 -> CategoryList(categories, onDelete = { id ->
                        scope.launch {
                            try {
                                service.deleteCategory(SupabaseClient.API_KEY, SupabaseClient.authHeader, "eq.$id")
                                loadData()
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    })
                    2 -> RepoList(repositories, categories, onDelete = { id ->
                        scope.launch {
                            try {
                                service.deleteRepository(SupabaseClient.API_KEY, SupabaseClient.authHeader, "eq.$id")
                                loadData()
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    })
                    3 -> GameSettingsAdminScreen(service)
                    4 -> appConfig?.let { config ->
                        ConfigScreen(
                            config = config,
                            prefs = prefs,
                            onLogout = {
                                prefs.edit().remove("auth_token").apply()
                                (context as? android.app.Activity)?.recreate()
                            },
                            onUpdate = { updated ->
                                scope.launch {
                                    try {
                                        service.updateAppConfig(SupabaseClient.API_KEY, SupabaseClient.authHeader, "eq.1", updated)
                                        loadData()
                                    } catch (e: Exception) { e.printStackTrace() }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryList(categories: List<SupabaseCategory>, onDelete: (Int) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { cat ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Category, null)
                    Spacer(Modifier.width(16.dp))
                    Text(cat.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { cat.id?.let { onDelete(it) } }) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun RepoList(repos: List<SupabaseRepo>, categories: List<SupabaseCategory>, onDelete: (Int) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(repos) { repo ->
            val catName = categories.find { it.id == repo.categoryId }?.name ?: "Sem Categoria"
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(repo.name, style = MaterialTheme.typography.titleMedium)
                        Text("${repo.owner}/${repo.repo}", style = MaterialTheme.typography.bodySmall)
                        Text("Categoria: $catName", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { repo.id?.let { onDelete(it) } }) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigScreen(config: AppConfig, prefs: android.content.SharedPreferences, onLogout: () -> Unit, onUpdate: (AppConfig) -> Unit) {
    var title by remember { mutableStateOf(config.dialogTitle ?: "") }
    var message by remember { mutableStateOf(config.dialogMessage ?: "") }
    var showDialog by remember { mutableStateOf(config.showDialog ?: false) }
    var isUpdate by remember { mutableStateOf(config.isUpdate ?: false) }
    var updateUrl by remember { mutableStateOf(config.updateUrl ?: "") }

    var mgmtToken by remember { mutableStateOf(prefs.getString("mgmt_token", "") ?: "") }
    var projectRef by remember { mutableStateOf(prefs.getString("project_ref", "jbqaegcuitmqfwpsdazn") ?: "jbqaegcuitmqfwpsdazn") }

    Column(Modifier.padding(16.dp).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Startup Dialog", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth(), minLines = 3)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = showDialog, onCheckedChange = { showDialog = it })
            Text("Show Dialog on Hub Startup")
        }

        HorizontalDivider()

        Text("Configuração de Atualização", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isUpdate, onCheckedChange = { isUpdate = it })
            Text("Este é um diálogo de atualização?")
        }

        if (isUpdate) {
            OutlinedTextField(
                value = updateUrl,
                onValueChange = { updateUrl = it },
                label = { Text("URL do APK de Atualização") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://...") }
            )
        }

        HorizontalDivider()
        Text("Dashboard API (Experimental)", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = mgmtToken,
            onValueChange = { mgmtToken = it },
            label = { Text("Supabase Access Token (PAT)") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
        )
        OutlinedTextField(
            value = projectRef,
            onValueChange = { projectRef = it },
            label = { Text("Project Reference") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                prefs.edit()
                    .putString("mgmt_token", mgmtToken)
                    .putString("project_ref", projectRef)
                    .apply()

                onUpdate(config.copy(
                    dialogTitle = title,
                    dialogMessage = message,
                    showDialog = showDialog,
                    isUpdate = isUpdate,
                    updateUrl = updateUrl
                ))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar Todas as Configurações")
        }

        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.ExitToApp, null)
            Spacer(Modifier.width(8.dp))
            Text("Sair da Conta")
        }
    }
}

@Composable
fun DashboardScreen(service: SupabaseManagementService, prefs: android.content.SharedPreferences) {
    val scope = rememberCoroutineScope()
    var usage by remember { mutableStateOf<UsageData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val token = prefs.getString("mgmt_token", "") ?: ""
    val ref = prefs.getString("project_ref", "") ?: ""

    fun load() {
        if (token.isBlank() || ref.isBlank()) {
            error = "Configure o Token e o Project Ref nas configurações."
            return
        }
        scope.launch {
            isLoading = true
            error = null
            try {
                val resp = service.getProjectUsage("Bearer $token", ref)
                usage = resp.data
            } catch (e: Exception) {
                error = "Erro ao carregar dados: ${e.message}"
                e.printStackTrace()
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else if (error != null) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(error!!, textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = MaterialTheme.colorScheme.error)
        }
    } else {
        Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Uso do Projeto", style = MaterialTheme.typography.headlineSmall)

            usage?.let { u ->
                UsageItem("REST Requests", u.restRequests)
                UsageItem("Auth Requests", u.authRequests)
                UsageItem("Storage Requests", u.storageRequests)
                UsageItem("Realtime Requests", u.realtimeRequests)
                UsageItem("DB Size", u.dbSize)
                UsageItem("Auth Users", u.authUsers)
                UsageItem("Storage Size", u.storageSize)
            }

            Button(onClick = { load() }, modifier = Modifier.fillMaxWidth()) {
                Text("Atualizar")
            }
        }
    }
}

@Composable
fun UsageItem(label: String, stat: UsageStat?) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${stat?.usage ?: 0}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                if (stat?.limit != null && stat.limit > 0) {
                    Text(" / ${stat.limit}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
                }
            }
            if (stat?.limit != null && stat.limit > 0) {
                LinearProgressIndicator(
                    progress = (stat.usage.toFloat() / stat.limit.toFloat()).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSettingsAdminScreen(service: SupabaseService) {
    val scope = rememberCoroutineScope()
    var submissions by remember { mutableStateOf<List<SupabaseGameSetting>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun load() {
        scope.launch {
            isLoading = true
            try {
                submissions = service.getAllGameSettings(SupabaseClient.API_KEY, SupabaseClient.authHeader)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(submissions) { game ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(game.name, style = MaterialTheme.typography.titleMedium)
                                Text("Por: ${game.submittedBy}", style = MaterialTheme.typography.bodySmall)
                                Badge(containerColor = when(game.status) {
                                    "approved" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                    "rejected" -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.secondary
                                }) {
                                    Text(game.status.uppercase(), color = androidx.compose.ui.graphics.Color.White)
                                }
                            }

                            if (game.status == "pending") {
                                IconButton(onClick = {
                                    scope.launch {
                                        service.updateGameSetting(SupabaseClient.API_KEY, SupabaseClient.authHeader, "eq.${game.id}", mapOf("status" to "approved"))
                                        load()
                                    }
                                }) { Icon(Icons.Default.Check, null, tint = androidx.compose.ui.graphics.Color(0xFF4CAF50)) }

                                IconButton(onClick = {
                                    scope.launch {
                                        service.updateGameSetting(SupabaseClient.API_KEY, SupabaseClient.authHeader, "eq.${game.id}", mapOf("status" to "rejected"))
                                        load()
                                    }
                                }) { Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error) }
                            } else {
                                IconButton(onClick = {
                                    scope.launch {
                                        service.deleteGameSetting(SupabaseClient.API_KEY, SupabaseClient.authHeader, "eq.${game.id}")
                                        load()
                                    }
                                }) { Icon(Icons.Default.Delete, null) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova Categoria") },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }) },
        confirmButton = { Button(onClick = { if(name.isNotBlank()) onConfirm(name) }) { Text("Adicionar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRepoDialog(categories: List<SupabaseCategory>, onDismiss: () -> Unit, onConfirm: (String, String, String, String, Int?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var repo by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedCatId by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Repositório") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp).also {  }) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") })
                OutlinedTextField(value = owner, onValueChange = { owner = it }, label = { Text("Owner (GitHub)") })
                OutlinedTextField(value = repo, onValueChange = { repo = it }, label = { Text("Repo (GitHub)") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descrição") })

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCatId }?.name ?: "Selecionar Categoria",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCatId = cat.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, owner, repo, desc, selectedCatId) }) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
