package com.winlator.downloader.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

data class DownloadInfo(
    val id: Long,
    val title: String,
    val progress: Int,
    val status: Int,
    val totalSize: Long,
    val downloadedSize: Long
)

@Composable
fun DownloadScreen() {
    val context = LocalContext.current
    var downloads by remember { mutableStateOf<List<DownloadInfo>>(emptyList()) }

    LaunchedEffect(Unit) {
        while (true) {
            downloads = getDownloadList(context)
            delay(1000) // Update every second
        }
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("Meus Downloads") })
        }
    ) { padding ->
        if (downloads.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("Nenhum download encontrado", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(downloads) { download ->
                    DownloadItem(download)
                }
            }
        }
    }
}

@Composable
fun DownloadItem(download: DownloadInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = download.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            val statusText = when (download.status) {
                DownloadManager.STATUS_PENDING -> "Pendente"
                DownloadManager.STATUS_RUNNING -> "Baixando..."
                DownloadManager.STATUS_PAUSED -> "Pausado"
                DownloadManager.STATUS_SUCCESSFUL -> "Concluído"
                DownloadManager.STATUS_FAILED -> "Falhou"
                else -> "Desconhecido"
            }

            Text(text = statusText, style = MaterialTheme.typography.bodySmall)

            if (download.status == DownloadManager.STATUS_RUNNING || download.status == DownloadManager.STATUS_PAUSED || download.status == DownloadManager.STATUS_PENDING) {
                LinearProgressIndicator(
                    progress = if (download.totalSize > 0) download.downloadedSize.toFloat() / download.totalSize else 0f,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                Text(
                    text = "${download.downloadedSize / (1024 * 1024)}MB / ${download.totalSize / (1024 * 1024)}MB",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

private fun getDownloadList(context: Context): List<DownloadInfo> {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val query = DownloadManager.Query()
    val cursor: Cursor = downloadManager.query(query)
    val list = mutableListOf<DownloadInfo>()

    if (cursor.moveToFirst()) {
        do {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE))
            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            val downloadedSize = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val totalSize = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

            val progress = if (totalSize > 0) ((downloadedSize * 100) / totalSize).toInt() else 0

            list.add(DownloadInfo(id, title ?: "Sem nome", progress, status, totalSize, downloadedSize))
        } while (cursor.moveToNext())
    }
    cursor.close()
    return list.sortedByDescending { it.id }
}
