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
import androidx.compose.ui.window.Dialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.winlator.downloader.data.GameSetting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSettingsScreen() {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var gameSettings by remember { mutableStateOf(loadGameSettings(context)) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedGame by remember { mutableStateOf<GameSetting?>(null) }

    val filteredGames = gameSettings.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações de Jogos") },
                actions = {
                    IconButton(onClick = {
                        backupGameSettings(context, gameSettings)
                        Toast.makeText(context, "Backup realizado!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Backup, contentDescription = "Backup")
                    }
                }
            )
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredGames) { game ->
                    GameCard(game = game, onClick = { selectedGame = game })
                }
            }
        }

        if (showAddDialog) {
            GameEditDialog(
                onDismiss = { showAddDialog = false },
                onSave = { newGame ->
                    gameSettings = gameSettings + newGame
                    saveGameSettings(context, gameSettings)
                    showAddDialog = false
                }
            )
        }

        if (selectedGame != null) {
            GameDetailDialog(
                game = selectedGame!!,
                onDismiss = { selectedGame = null },
                onDelete = {
                    gameSettings = gameSettings.filter { it.id != selectedGame!!.id }
                    saveGameSettings(context, gameSettings)
                    selectedGame = null
                }
            )
        }
    }
}

@Composable
fun GameCard(game: GameSetting, onClick: () -> Unit) {
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
                Text(text = game.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "${game.winlatorVersion} | ${game.gpuDriver}", style = MaterialTheme.typography.bodySmall)
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
fun GameDetailDialog(game: GameSetting, onDismiss: () -> Unit, onDelete: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "CONFIGURAÇÕES JOGO", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = "🎮 Nome: ${game.name}")
                Text(text = "🗂️ Formato: ${game.format}")
                Text(text = "📱 Dispositivo: ${game.device}")
                Text(text = "🎮 Gamepad Virtual: ${game.gamepad}")
                Text(text = "🪟 Winlator Versão: ${game.winlatorVersion}")
                Text(text = "📱 Gráfico do jogo: ${game.graphics}")
                HorizontalDivider()
                Text(text = "🍷 Wine: ${game.wine}")
                Text(text = "🔧 BOX64: ${game.box64}")
                Text(text = "🔧 BOX64 Preset: ${game.box64Preset}")
                HorizontalDivider()
                Text(text = "🔧 Edit Container")
                Text(text = "🔧 Resolução: ${game.resolution}")
                Text(text = "🔧 GPU Driver: ${game.gpuDriver}")
                Text(text = "🔧 DXVK/VKD3D: ${game.dxvk}")
                Text(text = "🔧 Áudio Driver: ${game.audioDriver}")

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("Excluir")
                    }
                    Button(onClick = onDismiss) { Text("Fechar") }
                }
            }
        }
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
