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

    // Form States
    var name by remember { mutableStateOf("") }
    var device by remember { mutableStateOf("") }
    var graphics by remember { mutableStateOf("") }
    var resolution by remember { mutableStateOf("") }
    var wineManual by remember { mutableStateOf("") }
    var box64Manual by remember { mutableStateOf("") }
    var gpuDriverManual by remember { mutableStateOf("") }
    var dxvkManual by remember { mutableStateOf("") }

    // Selection States
    var winlatorSel by remember { mutableStateOf(GitHubSelection()) }
    var wineSel by remember { mutableStateOf(GitHubSelection()) }
    var box64Sel by remember { mutableStateOf(GitHubSelection()) }
    var gpuDriverSel by remember { mutableStateOf(GitHubSelection()) }
    var dxvkSel by remember { mutableStateOf(GitHubSelection()) }

    var categories by remember { mutableStateOf<List<SupabaseCategory>>(emptyList()) }
    var repos by remember { mutableStateOf<List<SupabaseRepo>>(emptyList()) }

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
            categories = supabaseService.getCategories(SupabaseClient.API_KEY, SupabaseClient.AUTH)
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Informações do Jogo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome do Jogo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = device, onValueChange = { device = it }, label = { Text("Seu Dispositivo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = graphics, onValueChange = { graphics = it }, label = { Text("Configuração Gráfica") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Ex: Texturas Médio, Sombras Baixo") })
            OutlinedTextField(value = resolution, onValueChange = { resolution = it }, label = { Text("Resolução") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Ex: 854x480") })

            HorizontalDivider()
            GitHubFileSelector("Versão do Winlator", categories, repos, githubService) { winlatorSel = it }

            HorizontalDivider()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Wine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = wineManual, onValueChange = { wineManual = it }, label = { Text("Nome/Versão do Wine") }, modifier = Modifier.fillMaxWidth())
                GitHubFileSelector("Ou selecione um arquivo de Wine", categories, repos, githubService) { wineSel = it }
            }

            HorizontalDivider()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("BOX64", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = box64Manual, onValueChange = { box64Manual = it }, label = { Text("Versão do BOX64/Fexcore") }, modifier = Modifier.fillMaxWidth())
                GitHubFileSelector("Ou selecione um arquivo de BOX64/Fexcore", categories, repos, githubService) { box64Sel = it }
            }

            HorizontalDivider()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("GPU Driver", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = gpuDriverManual, onValueChange = { gpuDriverManual = it }, label = { Text("Nome do Driver") }, modifier = Modifier.fillMaxWidth())
                GitHubFileSelector("Ou selecione um arquivo de Driver", categories, repos, githubService) { gpuDriverSel = it }
            }

            HorizontalDivider()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("DXVK / VKD3D", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = dxvkManual, onValueChange = { dxvkManual = it }, label = { Text("Versão do DXVK") }, modifier = Modifier.fillMaxWidth())
                GitHubFileSelector("Ou selecione um arquivo de DXVK", categories, repos, githubService) { dxvkSel = it }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val newGame = GameSetting(
                            name = name, device = device, graphics = graphics,
                            winlatorVersion = winlatorSel.release?.tagName ?: "",
                            winlatorRepoOwner = winlatorSel.repo?.owner ?: "",
                            winlatorRepoName = winlatorSel.repo?.repo ?: "",
                            winlatorTagName = winlatorSel.release?.tagName ?: "",
                            winlatorAssetName = winlatorSel.asset?.name ?: "",

                            wine = wineManual.ifBlank { wineSel.release?.tagName ?: "" },
                            wineRepoOwner = wineSel.repo?.owner ?: "",
                            wineRepoName = wineSel.repo?.repo ?: "",
                            wineTagName = wineSel.release?.tagName ?: "",
                            wineAssetName = wineSel.asset?.name ?: "",

                            box64 = box64Manual.ifBlank { box64Sel.release?.tagName ?: "" },
                            box64RepoOwner = box64Sel.repo?.owner ?: "",
                            box64RepoName = box64Sel.repo?.repo ?: "",
                            box64TagName = box64Sel.release?.tagName ?: "",
                            box64AssetName = box64Sel.asset?.name ?: "",

                            gpuDriver = gpuDriverManual.ifBlank { gpuDriverSel.release?.tagName ?: "" },
                            gpuDriverRepoOwner = gpuDriverSel.repo?.owner ?: "",
                            gpuDriverRepoName = gpuDriverSel.repo?.repo ?: "",
                            gpuDriverTagName = gpuDriverSel.release?.tagName ?: "",
                            gpuDriverAssetName = gpuDriverSel.asset?.name ?: "",

                            dxvk = dxvkManual.ifBlank { dxvkSel.release?.tagName ?: "" },
                            dxvkRepoOwner = dxvkSel.repo?.owner ?: "",
                            dxvkRepoName = dxvkSel.repo?.repo ?: "",
                            dxvkTagName = dxvkSel.release?.tagName ?: "",
                            dxvkAssetName = dxvkSel.asset?.name ?: ""
                        )
                        // Save locally
                        val current = loadGameSettings(context)
                        saveGameSettings(context, current + newGame)

                        Toast.makeText(context, "Configuração salva!", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(16.dp)
            ) {
                Text("SALVAR CONFIGURAÇÃO")
            }
        }
    }
}
