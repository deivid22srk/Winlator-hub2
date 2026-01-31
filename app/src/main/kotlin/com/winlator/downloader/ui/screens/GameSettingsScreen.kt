package com.winlator.downloader.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.winlator.downloader.data.GameSetting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSettingsScreen(onAddGame: () -> Unit, onViewDetails: (com.winlator.downloader.data.SupabaseGameSetting) -> Unit) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var localGames by remember { mutableStateOf(loadGameSettings(context)) }
    var cloudGames by remember { mutableStateOf<List<com.winlator.downloader.data.SupabaseGameSetting>>(emptyList()) }
    var tabIndex by remember { mutableIntStateOf(0) }
    var isLoadingCloud by remember { mutableStateOf(false) }

    val supabaseService = remember {
        retrofit2.Retrofit.Builder()
            .baseUrl(com.winlator.downloader.data.SupabaseClient.URL)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(com.winlator.downloader.data.SupabaseService::class.java)
    }

    LaunchedEffect(tabIndex) {
        if (tabIndex == 1) {
            isLoadingCloud = true
            try {
                cloudGames = supabaseService.getApprovedGameSettings(
                    com.winlator.downloader.data.SupabaseClient.API_KEY,
                    com.winlator.downloader.data.SupabaseClient.AUTH
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isLoadingCloud = false
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Configurações de Jogos") },
                    actions = {
                        if (tabIndex == 0) {
                            IconButton(onClick = {
                                backupGameSettings(context, localGames)
                                Toast.makeText(context, "Backup realizado!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Backup, contentDescription = "Backup")
                            }
                        }
                    }
                )
                TabRow(selectedTabIndex = tabIndex) {
                    Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Meus Jogos") })
                    Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Comunidade") })
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddGame) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Jogo")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Pesquisar jogo...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            if (tabIndex == 0) {
                val filteredGames = localGames.filter { it.name.contains(searchQuery, ignoreCase = true) }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredGames) { game ->
                        GameCard(name = game.name, subtitle = game.winlatorVersion, onClick = {
                            onViewDetails(com.winlator.downloader.data.SupabaseGameSetting(
                                name = game.name, device = game.device, graphics = game.graphics,
                                winlatorVersion = game.winlatorVersion, winlatorRepoOwner = game.winlatorRepoOwner,
                                winlatorRepoName = game.winlatorRepoName, winlatorTagName = game.winlatorTagName,
                                winlatorAssetName = game.winlatorAssetName, wine = game.wine,
                                wineRepoOwner = game.wineRepoOwner, wineRepoName = game.wineRepoName,
                                wineTagName = game.wineTagName, wineAssetName = game.wineAssetName,
                                box64 = game.box64, box64RepoOwner = game.box64RepoOwner,
                                box64RepoName = game.box64RepoName, box64TagName = game.box64TagName,
                                box64AssetName = game.box64AssetName, gpuDriver = game.gpuDriver,
                                gpuDriverRepoOwner = game.gpuDriverRepoOwner, gpuDriverRepoName = game.gpuDriverRepoName,
                                gpuDriverTagName = game.gpuDriverTagName, gpuDriverAssetName = game.gpuDriverAssetName,
                                dxvk = game.dxvk, dxvkRepoOwner = game.dxvkRepoOwner,
                                dxvkRepoName = game.dxvkRepoName, dxvkTagName = game.dxvkTagName,
                                dxvkAssetName = game.dxvkAssetName, format = game.format,
                                gamepad = game.gamepad, resolution = game.resolution, audioDriver = game.audioDriver,
                                status = "local"
                            ))
                        })
                    }
                }
            } else {
                if (isLoadingCloud) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else {
                    val filteredGames = cloudGames.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredGames) { game ->
                            GameCard(name = game.name, subtitle = "Por: ${game.submittedBy}", onClick = { onViewDetails(game) })
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun GameCard(name: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.VideogameAsset, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}


@Composable
fun GameDetailDialog(
    name: String, format: String, device: String, gamepad: String, winlatorVersion: String,
    winlatorRepoOwner: String, winlatorRepoName: String, winlatorTagName: String, winlatorAssetName: String,
    graphics: String, wine: String, box64: String, box64Preset: String, resolution: String,
    gpuDriver: String, dxvk: String, audioDriver: String, submittedBy: String, youtubeUrl: String,
    isCloud: Boolean, onDismiss: () -> Unit, onDelete: (() -> Unit)? = null,
    onSubmitToCloud: ((String, String, String) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSubmitDialog by remember { mutableStateOf(false) }

    val githubService = remember {
        retrofit2.Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(com.winlator.downloader.data.GitHubService::class.java)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "CONFIGURAÇÕES JOGO", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = "🎮 Nome: $name")
                Text(text = "🗂️ Formato: $format")
                Text(text = "📱 Dispositivo: $device")
                Text(text = "🎮 Gamepad Virtual: $gamepad")
                Text(text = "🪟 Winlator Versão: $winlatorVersion")

                if (winlatorRepoOwner.isNotBlank() && winlatorRepoName.isNotBlank() && winlatorTagName.isNotBlank()) {
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val releases = githubService.getReleases(winlatorRepoOwner, winlatorRepoName)
                                    val release = releases.find { it.tagName == winlatorTagName }
                                    val asset = release?.assets?.find { it.name == winlatorAssetName } ?: release?.assets?.firstOrNull { it.name.endsWith(".apk") }

                                    if (asset != null) {
                                        downloadFile(context, asset.downloadUrl, asset.name)
                                    } else {
                                        Toast.makeText(context, "Arquivo não encontrado", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Erro ao buscar versão", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.Download, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Baixar esta versão")
                    }
                }

                Text(text = "📱 Gráfico do jogo: $graphics")

                if (isCloud && submittedBy.isNotBlank()) {
                    Text(text = "👤 Enviado por: $submittedBy", color = MaterialTheme.colorScheme.secondary)
                    if (youtubeUrl.isNotBlank()) {
                        Text(text = "📺 YouTube: $youtubeUrl", color = MaterialTheme.colorScheme.primary)
                    }
                }

                HorizontalDivider()
                Text(text = "🍷 Wine: $wine")
                Text(text = "🔧 BOX64: $box64")
                Text(text = "🔧 BOX64 Preset: $box64Preset")
                HorizontalDivider()
                Text(text = "🔧 Edit Container")
                Text(text = "🔧 Resolução: $resolution")
                Text(text = "🔧 GPU Driver: $gpuDriver")
                Text(text = "🔧 DXVK/VKD3D: $dxvk")
                Text(text = "🔧 Áudio Driver: $audioDriver")

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isCloud && onSubmitToCloud != null) {
                        Button(onClick = { showSubmitDialog = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.CloudUpload, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Enviar para Comunidade")
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        if (onDelete != null) {
                            TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                                Text("Excluir")
                            }
                        }
                        Button(onClick = onDismiss) { Text("Fechar") }
                    }
                }
            }
        }
    }

    if (showSubmitDialog) {
        var senderName by remember { mutableStateOf("") }
        var videoUrl by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showSubmitDialog = false },
            title = { Text("Enviar para Análise") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Preencha seu nome e opcionalmente um link de vídeo.")
                    OutlinedTextField(value = senderName, onValueChange = { senderName = it }, label = { Text("Seu Nome") })
                    OutlinedTextField(value = videoUrl, onValueChange = { videoUrl = it }, label = { Text("URL YouTube (Opcional)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (senderName.isNotBlank()) {
                        onSubmitToCloud?.invoke(name, senderName, videoUrl)
                        showSubmitDialog = false
                    }
                }) { Text("Enviar") }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

fun saveGameSettings(context: Context, settings: List<GameSetting>) {
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    val json = Gson().toJson(settings)
    prefs.edit().putString("settings_json", json).apply()
}

fun loadGameSettings(context: Context): List<GameSetting> {
    val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
    val json = prefs.getString("settings_json", null) ?: return emptyList()
    val type = object : TypeToken<List<GameSetting>>() {}.type
    return try {
        Gson().fromJson(json, type)
    } catch (e: Exception) {
        emptyList()
    }
}

fun backupGameSettings(context: Context, settings: List<GameSetting>) {
    // In a real app, this would write to a file or cloud. For now, we reuse SharedPreferences as "backup"
    val prefs = context.getSharedPreferences("game_prefs_backup", Context.MODE_PRIVATE)
    val json = Gson().toJson(settings)
    prefs.edit().putString("backup_json", json).apply()
}
