package com.winlator.downloader.ui.screens

import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.viewinterop.AndroidView
import com.winlator.downloader.data.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.widget.Toast
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailsScreen(
    game: SupabaseGameSetting,
    onBack: () -> Unit,
    onDeleteLocal: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val githubService = remember {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubService::class.java)
    }

    var showSubmitDialog by remember { mutableStateOf(false) }
    val supabaseService = remember {
        Retrofit.Builder()
            .baseUrl(SupabaseClient.URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseService::class.java)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(game.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                actions = {
                    if (onDeleteLocal != null) {
                        IconButton(onClick = onDeleteLocal) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(scrollState).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // YouTube Player
            if (game.youtubeUrl.isNotBlank()) {
                val videoId = extractYoutubeId(game.youtubeUrl)
                if (videoId != null) {
                    Card(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                        YouTubePlayer(videoId)
                    }
                }
            }

            Text("CONFIGURAÇÕES DO JOGO", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            InfoCard(title = "Dispositivo", content = game.device, icon = Icons.Default.PhoneAndroid)
            InfoCard(title = "Gráficos", content = game.graphics, icon = Icons.Default.GraphicEq)

            HorizontalDivider()

            // Components with Download buttons
            ComponentDownloadItem("Winlator", game.winlatorVersion, game.winlatorRepoOwner, game.winlatorRepoName, game.winlatorTagName, game.winlatorAssetName, githubService)
            ComponentDownloadItem("Wine", game.wine, game.wineRepoOwner, game.wineRepoName, game.wineTagName, game.wineAssetName, githubService)
            ComponentDownloadItem("BOX64", game.box64, game.box64RepoOwner, game.box64RepoName, game.box64TagName, game.box64AssetName, githubService)
            ComponentDownloadItem("GPU Driver", game.gpuDriver, game.gpuDriverRepoOwner, game.gpuDriverRepoName, game.gpuDriverTagName, game.gpuDriverAssetName, githubService)
            ComponentDownloadItem("DXVK", game.dxvk, game.dxvkRepoOwner, game.dxvkRepoName, game.dxvkTagName, game.dxvkAssetName, githubService)

            HorizontalDivider()

            Text("Outras Informações", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Resolução: ${game.resolution}")
            Text("Áudio: ${game.audioDriver}")

            if (game.status == "local") {
                Button(onClick = { showSubmitDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CloudUpload, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Enviar para Comunidade")
                }
            }

            if (game.submittedBy.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                    Text("Enviado por: ${game.submittedBy}", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    if (showSubmitDialog) {
        var senderName by remember { mutableStateOf("") }
        var videoUrl by remember { mutableStateOf(game.youtubeUrl) }

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
                        scope.launch {
                            try {
                                supabaseService.submitGameSetting(
                                    SupabaseClient.API_KEY, SupabaseClient.AUTH,
                                    game.copy(submittedBy = senderName, youtubeUrl = videoUrl, status = "pending")
                                )
                                Toast.makeText(context, "Enviado!", Toast.LENGTH_SHORT).show()
                                showSubmitDialog = false
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erro ao enviar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) { Text("Enviar") }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun InfoCard(title: String, content: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall)
                Text(content.ifBlank { "Não informado" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun ComponentDownloadItem(
    label: String,
    versionName: String,
    owner: String,
    repo: String,
    tag: String,
    asset: String,
    service: GitHubService
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(versionName.ifBlank { "Padrão" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
            if (owner.isNotBlank() && repo.isNotBlank() && tag.isNotBlank()) {
                IconButton(onClick = {
                    scope.launch {
                        try {
                            val releases = service.getReleases(owner, repo)
                            val release = releases.find { it.tagName == tag }
                            val targetAsset = release?.assets?.find { it.name == asset } ?: release?.assets?.firstOrNull()
                            if (targetAsset != null) {
                                downloadFile(context, targetAsset.downloadUrl, targetAsset.name)
                            } else {
                                Toast.makeText(context, "Arquivo não encontrado", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erro ao buscar versão", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun YouTubePlayer(videoId: String) {
    AndroidView(factory = { context ->
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            // Using a simple HTML to avoid some YouTube embedding issues
            val html = """
                <html>
                    <body style="margin:0;padding:0;">
                        <iframe width="100%" height="100%" src="https://www.youtube.com/embed/$videoId" frameborder="0" allowfullscreen></iframe>
                    </body>
                </html>
            """.trimIndent()
            loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "UTF-8", null)
        }
    }, modifier = Modifier.fillMaxSize())
}

fun extractYoutubeId(url: String): String? {
    return try {
        if (url.contains("v=")) {
            url.split("v=")[1].split("&")[0]
        } else if (url.contains("youtu.be/")) {
            url.split("youtu.be/")[1].split("?")[0]
        } else if (url.contains("embed/")) {
            url.split("embed/")[1].split("?")[0]
        } else null
    } catch (e: Exception) { null }
}
