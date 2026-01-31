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
fun GameSettingsScreen() {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var localGames by remember { mutableStateOf(loadGameSettings(context)) }
    var cloudGames by remember { mutableStateOf<List<com.winlator.downloader.data.SupabaseGameSetting>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedLocalGame by remember { mutableStateOf<GameSetting?>(null) }
    var selectedCloudGame by remember { mutableStateOf<com.winlator.downloader.data.SupabaseGameSetting?>(null) }
    var tabIndex by remember { mutableIntStateOf(0) }
    var isLoadingCloud by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
            FloatingActionButton(onClick = { showAddDialog = true }) {
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
                        GameCard(name = game.name, subtitle = game.winlatorVersion, onClick = { selectedLocalGame = game })
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
                            GameCard(name = game.name, subtitle = "Por: ${game.submittedBy}", onClick = { selectedCloudGame = game })
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            GameEditDialog(
                onDismiss = { showAddDialog = false },
                onSave = { newGame ->
                    localGames = localGames + newGame
                    saveGameSettings(context, localGames)
                    showAddDialog = false
                }
            )
        }

        if (selectedLocalGame != null) {
            GameDetailDialog(
                name = selectedLocalGame!!.name,
                format = selectedLocalGame!!.format,
                device = selectedLocalGame!!.device,
                gamepad = selectedLocalGame!!.gamepad,
                winlatorVersion = selectedLocalGame!!.winlatorVersion,
                graphics = selectedLocalGame!!.graphics,
                wine = selectedLocalGame!!.wine,
                box64 = selectedLocalGame!!.box64,
                box64Preset = selectedLocalGame!!.box64Preset,
                resolution = selectedLocalGame!!.resolution,
                gpuDriver = selectedLocalGame!!.gpuDriver,
                dxvk = selectedLocalGame!!.dxvk,
                audioDriver = selectedLocalGame!!.audioDriver,
                submittedBy = "",
                youtubeUrl = "",
                isCloud = false,
                onDismiss = { selectedLocalGame = null },
                onDelete = {
                    localGames = localGames.filter { it.id != selectedLocalGame!!.id }
                    saveGameSettings(context, localGames)
                    selectedLocalGame = null
                },
                onSubmitToCloud = { _, submittedBy, youtubeUrl ->
                    scope.launch {
                        try {
                            val g = selectedLocalGame!!
                            supabaseService.submitGameSetting(
                                com.winlator.downloader.data.SupabaseClient.API_KEY,
                                com.winlator.downloader.data.SupabaseClient.AUTH,
                                com.winlator.downloader.data.SupabaseGameSetting(
                                    name = g.name, format = g.format, device = g.device, gamepad = g.gamepad,
                                    winlatorVersion = g.winlatorVersion, graphics = g.graphics, wine = g.wine,
                                    box64 = g.box64, box64Preset = g.box64Preset, resolution = g.resolution,
                                    gpuDriver = g.gpuDriver, dxvk = g.dxvk, audioDriver = g.audioDriver,
                                    submittedBy = submittedBy, youtubeUrl = youtubeUrl
                                )
                            )
                            Toast.makeText(context, "Enviado para análise!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro ao enviar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        if (selectedCloudGame != null) {
            val g = selectedCloudGame!!
            GameDetailDialog(
                name = g.name, format = g.format, device = g.device, gamepad = g.gamepad,
                winlatorVersion = g.winlatorVersion, graphics = g.graphics, wine = g.wine,
                box64 = g.box64, box64Preset = g.box64Preset, resolution = g.resolution,
                gpuDriver = g.gpuDriver, dxvk = g.dxvk, audioDriver = g.audioDriver,
                submittedBy = g.submittedBy, youtubeUrl = g.youtubeUrl,
                isCloud = true,
                onDismiss = { selectedCloudGame = null }
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameEditDialog(onDismiss: () -> Unit, onSave: (GameSetting) -> Unit) {
    var name by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("Pré instalado") }
    var device by remember { mutableStateOf("") }
    var gamepad by remember { mutableStateOf("Não") }
    var winlatorVersion by remember { mutableStateOf("") }
    var graphics by remember { mutableStateOf("") }
    var wine by remember { mutableStateOf("") }
    var box64 by remember { mutableStateOf("") }
    var box64Preset by remember { mutableStateOf("") }
    var resolution by remember { mutableStateOf("") }
    var gpuDriver by remember { mutableStateOf("") }
    var dxvk by remember { mutableStateOf("") }
    var audioDriver by remember { mutableStateOf("alsa") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Adicionar Jogo", style = MaterialTheme.typography.headlineSmall)

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome do Jogo") })
                OutlinedTextField(value = format, onValueChange = { format = it }, label = { Text("Formato") })
                OutlinedTextField(value = device, onValueChange = { device = it }, label = { Text("Dispositivo") })
                OutlinedTextField(value = gamepad, onValueChange = { gamepad = it }, label = { Text("Gamepad Virtual") })
                OutlinedTextField(value = winlatorVersion, onValueChange = { winlatorVersion = it }, label = { Text("Winlator Versão") })
                OutlinedTextField(value = graphics, onValueChange = { graphics = it }, label = { Text("Gráficos") })
                OutlinedTextField(value = wine, onValueChange = { wine = it }, label = { Text("Wine") })
                OutlinedTextField(value = box64, onValueChange = { box64 = it }, label = { Text("BOX64") })
                OutlinedTextField(value = box64Preset, onValueChange = { box64Preset = it }, label = { Text("BOX64 Preset") })
                OutlinedTextField(value = resolution, onValueChange = { resolution = it }, label = { Text("Resolução") })
                OutlinedTextField(value = gpuDriver, onValueChange = { gpuDriver = it }, label = { Text("GPU Driver") })
                OutlinedTextField(value = dxvk, onValueChange = { dxvk = it }, label = { Text("DXVK/VKD3D") })
                OutlinedTextField(value = audioDriver, onValueChange = { audioDriver = it }, label = { Text("Áudio Driver") })

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(onClick = {
                        if (name.isNotBlank()) {
                            onSave(GameSetting(
                                name = name, format = format, device = device, gamepad = gamepad,
                                winlatorVersion = winlatorVersion, graphics = graphics, wine = wine,
                                box64 = box64, box64Preset = box64Preset, resolution = resolution,
                                gpuDriver = gpuDriver, dxvk = dxvk, audioDriver = audioDriver
                            ))
                        }
                    }) { Text("Salvar") }
                }
            }
        }
    }
}

@Composable
fun GameDetailDialog(
    name: String, format: String, device: String, gamepad: String, winlatorVersion: String,
    graphics: String, wine: String, box64: String, box64Preset: String, resolution: String,
    gpuDriver: String, dxvk: String, audioDriver: String, submittedBy: String, youtubeUrl: String,
    isCloud: Boolean, onDismiss: () -> Unit, onDelete: (() -> Unit)? = null,
    onSubmitToCloud: ((String, String, String) -> Unit)? = null
) {
    var showSubmitDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "CONFIGURAÇÕES JOGO", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = "🎮 Nome: $name")
                Text(text = "🗂️ Formato: $format")
                Text(text = "📱 Dispositivo: $device")
                Text(text = "🎮 Gamepad Virtual: $gamepad")
                Text(text = "🪟 Winlator Versão: $winlatorVersion")
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
