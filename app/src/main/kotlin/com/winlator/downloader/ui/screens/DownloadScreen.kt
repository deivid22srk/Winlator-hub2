package com.winlator.downloader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.winlator.downloader.data.AppDownloadManager
import com.winlator.downloader.data.DownloadStatus
import com.winlator.downloader.data.DownloadTask

@Composable
fun DownloadScreen() {
    val tasks = AppDownloadManager.tasks
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Gerenciador de Downloads") })
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("Nenhum download ativo", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(tasks) { task ->
                    DownloadTaskItem(task)
                }
            }
        }
    }
}

@Composable
fun DownloadTaskItem(task: DownloadTask) {
    val progress by task.progress.collectAsState()
    val status by task.status.collectAsState()
    val scope = rememberCoroutineScope()

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f).height(8.dp),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = when(status) {
                        DownloadStatus.IDLE -> "Aguardando"
                        DownloadStatus.QUEUED -> "Na fila"
                        DownloadStatus.DOWNLOADING -> "Baixando..."
                        DownloadStatus.PAUSED -> "Pausado"
                        DownloadStatus.COMPLETED -> "Concluído"
                        DownloadStatus.FAILED -> "Falhou"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (status == DownloadStatus.FAILED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row {
                    if (status == DownloadStatus.DOWNLOADING || status == DownloadStatus.QUEUED) {
                        IconButton(onClick = { task.pause() }) {
                            Icon(Icons.Default.Pause, contentDescription = "Pausar")
                        }
                    } else if (status == DownloadStatus.PAUSED || status == DownloadStatus.FAILED) {
                        IconButton(onClick = { task.resume(scope) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Retomar")
                        }
                    }

                    if (status == DownloadStatus.COMPLETED) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Concluído", tint = androidx.compose.ui.graphics.Color(0xFF4CAF50))
                    }
                }
            }
        }
    }
}
