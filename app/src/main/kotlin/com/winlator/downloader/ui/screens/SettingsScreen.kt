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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var downloadPath by remember { mutableStateOf(getDownloadPath(context)) }

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

            Text(text = "Código Aberto", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Text(
                text = "Este aplicativo é de código aberto e você pode contribuir ou ver o código no GitHub.",
                style = MaterialTheme.typography.bodyMedium
            )

            val intent = remember {
                android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/deivid22srk/Winlator-hub2"))
            }
            Button(
                onClick = { context.startActivity(intent) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Link, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver no GitHub")
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
