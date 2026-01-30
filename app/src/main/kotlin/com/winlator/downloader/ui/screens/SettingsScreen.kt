package com.winlator.downloader.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.winlator.downloader.data.GameSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var downloadPath by remember { mutableStateOf(getDownloadPath(context)) }
    var repoUrl by remember { mutableStateOf(getRepoUrl(context)) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Configurações") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(text = "Geral", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = downloadPath,
                onValueChange = {
                    downloadPath = it
                    saveDownloadPath(context, it)
                },
                label = { Text("Subpasta de Downloads") },
                placeholder = { Text("Ex: WinlatorDownloads") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
                supportingText = { Text("Arquivos serão salvos em Downloads/$downloadPath") }
            )

            HorizontalDivider()

            Text(text = "Repositório de Jogos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = repoUrl,
                onValueChange = {
                    repoUrl = it
                    saveRepoUrl(context, it)
                },
                label = { Text("URL do Repositório (JSON)") },
                placeholder = { Text("https://exemplo.com/jogos.json") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) }
            )

            Button(
                onClick = {
                    importGameSettingsFromUrl(context, repoUrl) {
                        // Refresh logic could go here
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Importar Configurações")
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Os arquivos baixados podem ser acessados pelo seu gerenciador de arquivos na pasta Downloads.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Versão 1.0",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

fun saveDownloadPath(context: Context, path: String) {
    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("download_path", path).apply()
}

fun getDownloadPath(context: Context): String {
    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    return prefs.getString("download_path", "WinlatorHub") ?: "WinlatorHub"
}

fun saveRepoUrl(context: Context, url: String) {
    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("repo_url", url).apply()
}

fun getRepoUrl(context: Context): String {
    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    return prefs.getString("repo_url", "") ?: ""
}

fun importGameSettingsFromUrl(context: Context, url: String, onComplete: () -> Unit) {
    if (url.isBlank() || !url.startsWith("http")) {
        Toast.makeText(context, "URL inválida", Toast.LENGTH_SHORT).show()
        return
    }

    val scope = kotlinx.coroutines.MainScope()
    scope.launch {
        Toast.makeText(context, "Buscando configurações...", Toast.LENGTH_SHORT).show()
        val result = withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) null
                    else response.body?.string()
                }
            } catch (e: Exception) {
                null
            }
        }

        if (result != null) {
            try {
                val type = object : TypeToken<List<GameSetting>>() {}.type
                val importedSettings: List<GameSetting> = Gson().fromJson(result, type)
                val currentSettings = loadGameSettings(context)
                val updatedSettings = (currentSettings + importedSettings).distinctBy { it.name }
                saveGameSettings(context, updatedSettings)
                Toast.makeText(context, "${importedSettings.size} configurações importadas!", Toast.LENGTH_LONG).show()
                onComplete()
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao processar JSON", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Erro ao baixar arquivo", Toast.LENGTH_SHORT).show()
        }
    }
}
