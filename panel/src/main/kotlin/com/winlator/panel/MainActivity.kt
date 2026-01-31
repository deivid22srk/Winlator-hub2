package com.winlator.panel

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.winlator.panel.data.*
import com.winlator.panel.ui.theme.WinlatorPanelTheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WinlatorPanelTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PanelMainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelMainScreen() {
    val scope = rememberCoroutineScope()
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

    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                repositories = service.getRepositories(SupabaseClient.API_KEY, SupabaseClient.AUTH)
                appConfig = service.getAppConfig(SupabaseClient.API_KEY, SupabaseClient.AUTH).firstOrNull()
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
                    icon = { Icon(Icons.Default.List, null) },
                    label = { Text("Repos") },
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Config") },
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 }
                )
            }
        },
        floatingActionButton = {
            if (tabIndex == 0) {
                var showAddDialog by remember { mutableStateOf(false) }
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, null)
                }
                if (showAddDialog) {
                    AddRepoDialog(
                        onDismiss = { showAddDialog = false },
                        onConfirm = { name, owner, repo, desc ->
                            scope.launch {
                                try {
                                    service.createRepository(
                                        SupabaseClient.API_KEY, SupabaseClient.AUTH,
                                        SupabaseRepo(name = name, owner = owner, repo = repo, description = desc)
                                    )
                                    loadData()
                                    showAddDialog = false
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    )
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
                if (tabIndex == 0) {
                    RepoList(repositories, onDelete = { id ->
                        scope.launch {
                            try {
                                service.deleteRepository(SupabaseClient.API_KEY, SupabaseClient.AUTH, "eq.$id")
                                loadData()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    })
                } else {
                    appConfig?.let { config ->
                        ConfigScreen(config, onUpdate = { updated ->
                            scope.launch {
                                try {
                                    service.updateAppConfig(SupabaseClient.API_KEY, SupabaseClient.AUTH, "eq.1", updated)
                                    loadData()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun RepoList(repos: List<SupabaseRepo>, onDelete: (Int) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(repos) { repo ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(repo.name, style = MaterialTheme.typography.titleMedium)
                        Text("${repo.owner}/${repo.repo}", style = MaterialTheme.typography.bodySmall)
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
fun ConfigScreen(config: AppConfig, onUpdate: (AppConfig) -> Unit) {
    var title by remember { mutableStateOf(config.dialogTitle) }
    var message by remember { mutableStateOf(config.dialogMessage) }
    var showDialog by remember { mutableStateOf(config.showDialog) }

    Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Startup Dialog", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = showDialog, onCheckedChange = { showDialog = it })
            Text("Show Dialog on Hub Startup")
        }
        Button(onClick = { onUpdate(config.copy(dialogTitle = title, dialogMessage = message, showDialog = showDialog)) }, modifier = Modifier.fillMaxWidth()) {
            Text("Salvar Configuração")
        }
    }
}

@Composable
fun AddRepoDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var repo by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Repositório") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") })
                OutlinedTextField(value = owner, onValueChange = { owner = it }, label = { Text("Owner (GitHub)") })
                OutlinedTextField(value = repo, onValueChange = { repo = it }, label = { Text("Repo (GitHub)") })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Descrição") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, owner, repo, desc) }) { Text("Adicionar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
