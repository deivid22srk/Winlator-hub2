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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.winlator.downloader.data.*
import com.winlator.downloader.utils.UserUtils
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
    val userId = remember { UserUtils.getUserId(context) }
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

    var likes by remember { mutableIntStateOf(game.likesCount) }
    var dislikes by remember { mutableIntStateOf(game.dislikesCount) }
    var userVote by remember { mutableIntStateOf(0) } // 1: like, -1: dislike, 0: none

    LaunchedEffect(game.id) {
        if (game.id != null) {
            try {
                val votes = supabaseService.getUserVote(SupabaseClient.API_KEY, SupabaseClient.AUTH, "eq.${game.id}", "eq.$userId")
                userVote = votes.firstOrNull()?.voteType ?: 0
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun handleVote(type: Int) {
        if (game.id == null) return
        scope.launch {
            try {
                val oldVote = userVote
                if (userVote == type) {
                    // Remove vote
                    val resp = supabaseService.deleteVote(SupabaseClient.API_KEY, SupabaseClient.AUTH, "eq.${game.id}", "eq.$userId")
                    if (resp.isSuccessful) {
                        if (type == 1) likes-- else dislikes--
                        userVote = 0
                    } else {
                        Toast.makeText(context, "Erro ao remover voto: ${resp.code()}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Add/Change vote
                    val resp = supabaseService.vote(SupabaseClient.API_KEY, SupabaseClient.AUTH, vote = SupabaseVote(game.id!!, userId, type))
                    if (resp.isSuccessful) {
                        if (type == 1) likes++ else dislikes++
                        if (oldVote == 1) likes-- else if (oldVote == -1) dislikes--
                        userVote = type
                    } else {
                        val errorMsg = resp.errorBody()?.string() ?: ""
                        android.util.Log.e("VoteError", "Error: ${resp.code()} $errorMsg")
                        Toast.makeText(context, "Erro ao votar: ${resp.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Erro de conexão", Toast.LENGTH_SHORT).show()
            }
        }
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("CONFIGURAÇÕES DO JOGO", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                if (game.status != "local") {
                    IconButton(onClick = { handleVote(1) }) {
                        Icon(if (userVote == 1) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt, null, tint = if (userVote == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    }
                    Text("$likes", style = MaterialTheme.typography.labelLarge)

                    IconButton(onClick = { handleVote(-1) }) {
                        Icon(if (userVote == -1) Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt, null, tint = if (userVote == -1) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                    }
                    Text("$dislikes", style = MaterialTheme.typography.labelLarge)
                }
            }

            InfoCard(title = "Dispositivo", content = game.device, icon = Icons.Default.PhoneAndroid)
            InfoCard(title = "Gráficos", content = game.graphics, icon = Icons.Default.GraphicEq)

            HorizontalDivider()

            // Components with Download buttons
            ComponentDownloadItem("Winlator", game.winlatorVersion, game.winlatorRepoOwner, game.winlatorRepoName, game.winlatorTagName, game.winlatorAssetName, githubService)
            ComponentDownloadItem("Wine", game.wine, game.wineRepoOwner, game.wineRepoName, game.wineTagName, game.wineAssetName, githubService)
            ComponentDownloadItem("BOX64/Fexcore", game.box64, game.box64RepoOwner, game.box64RepoName, game.box64TagName, game.box64AssetName, githubService)
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
                                val resp = supabaseService.submitGameSetting(
                                    SupabaseClient.API_KEY, SupabaseClient.AUTH,
                                    game.copy(submittedBy = senderName, youtubeUrl = videoUrl, status = "pending")
                                )
                                if (resp.isSuccessful) {
                                    Toast.makeText(context, "Enviado!", Toast.LENGTH_SHORT).show()
                                    showSubmitDialog = false
                                } else {
                                    Toast.makeText(context, "Erro: ${resp.code()}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erro ao enviar: ${e.message}", Toast.LENGTH_SHORT).show()
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
    val context = LocalContext.current
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            YouTubePlayerView(ctx).apply {
                lifecycleOwner.lifecycle.addObserver(this)
                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        youTubePlayer.cueVideo(videoId, 0f)
                    }
                })
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

fun extractYoutubeId(url: String): String? {
    if (url.isBlank()) return null
    val cleaned = url.trim()
    return try {
        // Most common pattern: https://youtu.be/zpguI-oucC0?si=...
        if (cleaned.contains("youtu.be/")) {
            cleaned.split("youtu.be/").last().split("?").first()
        }
        // Standard pattern: https://www.youtube.com/watch?v=...
        else if (cleaned.contains("v=")) {
            cleaned.split("v=").last().split("&").first()
        }
        // Embed pattern: https://www.youtube.com/embed/...
        else if (cleaned.contains("embed/")) {
            cleaned.split("embed/").last().split("?").first()
        }
        // Fallback with a more exhaustive regex
        else {
            val pattern = "^(?:https?:\\/\\/)?(?:www\\.|m\\.)?(?:youtu\\.be\\/|youtube\\.com\\/(?:embed\\/|v\\/|watch\\?v=|watch\\?.+&v=))((\\w|-){11})(?:\\S+)?$".toRegex()
            pattern.find(cleaned)?.groupValues?.get(1)
        }
    } catch (e: Exception) { null }
}
