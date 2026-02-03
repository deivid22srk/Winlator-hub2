package com.winlator.downloader.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var downloadPath by remember { mutableStateOf(getDownloadPath(context)) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Configurações", fontWeight = FontWeight.Bold) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSection(title = "Geral") {
                ListItem(
                    headlineContent = { Text("Subpasta de Downloads") },
                    supportingContent = {
                        OutlinedTextField(
                            value = downloadPath,
                            onValueChange = {
                                downloadPath = it
                                saveDownloadPath(context, it)
                            },
                            placeholder = { Text("Ex: WinlatorHub") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            shape = MaterialTheme.shapes.medium,
                            singleLine = true
                        )
                    },
                    leadingContent = { Icon(Icons.Default.Folder, null) }
                )
                Text(
                    "Arquivos salvos em Downloads/$downloadPath",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 56.dp, bottom = 16.dp)
                )
            }

            SettingsSection(title = "Comunidade e Código") {
                ListItem(
                    headlineContent = { Text("Código Aberto") },
                    supportingContent = { Text("Contribua ou veja o código no GitHub.") },
                    leadingContent = { Icon(Icons.Default.Info, null) },
                    trailingContent = {
                        val intent = remember {
                            android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/deivid22srk/Winlator-hub2"))
                        }
                        FilledTonalButton(onClick = { context.startActivity(intent) }) {
                            Text("GitHub")
                        }
                    }
                )

                ListItem(
                    headlineContent = { Text("Local de Download") },
                    supportingContent = { Text("Acesse via gerenciador de arquivos em Downloads.") },
                    leadingContent = { Icon(Icons.Default.Info, null) }
                )
            }

            SettingsSection(title = "Desenvolvedor") {
                val hailGamesUri = "https://youtube.com/@hail-games"
                val hailGamesIntent = remember {
                    android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(hailGamesUri))
                }

                ListItem(
                    modifier = Modifier.clickable { context.startActivity(hailGamesIntent) },
                    headlineContent = { Text("App feito por HailGames", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("Clique para visitar o canal no YouTube") },
                    leadingContent = {
                        AsyncImage(
                            model = "https://yt3.googleusercontent.com/3qxFKrt02JDd4J1gL2kjGeZF8cJjJ5AVt78e9YbCzv2bRWGT-flh0d-iE4iT21U7_WjpJosR=s160-c-k-c0x00ffffff-no-rj",
                            contentDescription = "HailGames Logo",
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                        )
                    },
                    trailingContent = { Icon(Icons.Default.Link, null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Winlator Hub Versão 1.0",
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        ElevatedCard(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
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
