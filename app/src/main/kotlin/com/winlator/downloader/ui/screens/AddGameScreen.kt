package com.winlator.downloader.ui.screens

import android.widget.Toast
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
import com.winlator.downloader.data.*
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGameScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Form States
    var name by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("Pré instalado") }
    var device by remember { mutableStateOf("") }
    var gamepad by remember { mutableStateOf("Não") }
    var graphics by remember { mutableStateOf("") }
    var wine by remember { mutableStateOf("") }
    var box64 by remember { mutableStateOf("") }
    var box64Preset by remember { mutableStateOf("") }
    var resolution by remember { mutableStateOf("") }
    var gpuDriver by remember { mutableStateOf("") }
    var dxvk by remember { mutableStateOf("") }
    var audioDriver by remember { mutableStateOf("alsa") }

    // Version Selector States
    var repos by remember { mutableStateOf<List<SupabaseRepo>>(emptyList()) }
    var selectedRepo by remember { mutableStateOf<SupabaseRepo?>(null) }
    var releases by remember { mutableStateOf<List<GitHubRelease>>(emptyList()) }
    var selectedRelease by remember { mutableStateOf<GitHubRelease?>(null) }
    var selectedAsset by remember { mutableStateOf<GitHubAsset?>(null) }

    var repoExpanded by remember { mutableStateOf(false) }
    var releaseExpanded by remember { mutableStateOf(false) }
    var assetExpanded by remember { mutableStateOf(false) }

    val supabaseService = remember {
        Retrofit.Builder()
            .baseUrl(SupabaseClient.URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseService::class.java)
    }

    val githubService = remember {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubService::class.java)
    }

    LaunchedEffect(Unit) {
        try {
            repos = supabaseService.getRepositories(SupabaseClient.API_KEY, SupabaseClient.AUTH)
        } catch (e: Exception) { e.printStackTrace() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adicionar Jogo") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Configurações Básicas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome do Jogo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = device, onValueChange = { device = it }, label = { Text("Dispositivo") }, modifier = Modifier.fillMaxWidth())

            HorizontalDivider()
            Text("Versão do Winlator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            // Repo Selector
            ExposedDropdownMenuBox(expanded = repoExpanded, onExpandedChange = { repoExpanded = !repoExpanded }) {
                OutlinedTextField(
                    value = selectedRepo?.name ?: "Selecionar Repositório",
                    onValueChange = {}, readOnly = true,
                    label = { Text("Repositório") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repoExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = repoExpanded, onDismissRequest = { repoExpanded = false }) {
                    repos.forEach { repo ->
                        DropdownMenuItem(
                            text = { Text(repo.name) },
                            onClick = {
                                selectedRepo = repo
                                selectedRelease = null
                                selectedAsset = null
                                repoExpanded = false
                                scope.launch {
                                    try {
                                        releases = githubService.getReleases(repo.owner, repo.repo)
                                    } catch (e: Exception) { e.printStackTrace() }
                                }
                            }
                        )
                    }
                }
            }

            // Release Selector
            if (selectedRepo != null) {
                ExposedDropdownMenuBox(expanded = releaseExpanded, onExpandedChange = { releaseExpanded = !releaseExpanded }) {
                    OutlinedTextField(
                        value = selectedRelease?.tagName ?: "Selecionar Versão (Tag)",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Versão") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = releaseExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = releaseExpanded, onDismissRequest = { releaseExpanded = false }) {
                        releases.forEach { release ->
                            DropdownMenuItem(
                                text = { Text(release.tagName) },
                                onClick = {
                                    selectedRelease = release
                                    selectedAsset = null
                                    releaseExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Asset Selector
            if (selectedRelease != null) {
                ExposedDropdownMenuBox(expanded = assetExpanded, onExpandedChange = { assetExpanded = !assetExpanded }) {
                    OutlinedTextField(
                        value = selectedAsset?.name ?: "Selecionar Arquivo (APK)",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Arquivo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assetExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = assetExpanded, onDismissRequest = { assetExpanded = false }) {
                        selectedRelease!!.assets.forEach { asset ->
                            DropdownMenuItem(
                                text = { Text(asset.name) },
                                onClick = {
                                    selectedAsset = asset
                                    assetExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider()
            Text("Configurações Avançadas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = wine, onValueChange = { wine = it }, label = { Text("Wine") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = box64, onValueChange = { box64 = it }, label = { Text("BOX64") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = gpuDriver, onValueChange = { gpuDriver = it }, label = { Text("GPU Driver") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = dxvk, onValueChange = { dxvk = it }, label = { Text("DXVK") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val newGame = GameSetting(
                            name = name, format = format, device = device, gamepad = gamepad,
                            winlatorVersion = selectedRelease?.tagName ?: "",
                            winlatorRepoOwner = selectedRepo?.owner ?: "",
                            winlatorRepoName = selectedRepo?.repo ?: "",
                            winlatorTagName = selectedRelease?.tagName ?: "",
                            winlatorAssetName = selectedAsset?.name ?: "",
                            graphics = graphics, wine = wine, box64 = box64, box64Preset = box64Preset,
                            resolution = resolution, gpuDriver = gpuDriver, dxvk = dxvk, audioDriver = audioDriver
                        )
                        // Save locally
                        val current = loadGameSettings(context)
                        saveGameSettings(context, current + newGame)

                        Toast.makeText(context, "Jogo adicionado!", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Salvar Jogo")
            }
        }
    }
}
