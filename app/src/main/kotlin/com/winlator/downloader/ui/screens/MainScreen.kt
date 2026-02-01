package com.winlator.downloader.ui.screens

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import android.app.Activity
import com.winlator.downloader.data.*
import com.winlator.downloader.navigation.NavigationItem
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var selectedGameDetail by remember { mutableStateOf<SupabaseGameSetting?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val items = listOf(
                    NavigationItem.Home,
                    NavigationItem.Downloads,
                    NavigationItem.GameSettings,
                    NavigationItem.Settings
                )

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavHost(navController, startDestination = NavigationItem.Home.route) {
                composable(NavigationItem.Home.route) { HomeScreen() }
                composable(NavigationItem.Downloads.route) { DownloadScreen() }
                composable(NavigationItem.GameSettings.route) {
                    GameSettingsScreen(
                        onAddGame = { navController.navigate(NavigationItem.AddGame.route) },
                        onViewDetails = { game ->
                            selectedGameDetail = game
                            navController.navigate(NavigationItem.GameDetails.route)
                        }
                    )
                }
                composable(NavigationItem.AddGame.route) { AddGameScreen(onBack = { navController.popBackStack() }) }
                composable(NavigationItem.GameDetails.route) {
                    val context = LocalContext.current
                    selectedGameDetail?.let { game ->
                        GameDetailsScreen(
                            game = game,
                            onBack = { navController.popBackStack() },
                            onDeleteLocal = if (game.status == "local") {
                                {
                                    val current = loadGameSettings(context)
                                    // Match by name for deletion of local items in this simplified demo
                                    saveGameSettings(context, current.filter { it.name != game.name })
                                    navController.popBackStack()
                                    Toast.makeText(context, "Excluído!", Toast.LENGTH_SHORT).show()
                                }
                            } else null
                        )
                    }
                }
                composable(NavigationItem.Settings.route) { SettingsScreen() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var selectedRepo by remember { mutableStateOf<WinlatorRepo?>(null) }
    var selectedCategory by remember { mutableStateOf<SupabaseCategory?>(null) }
    var categories by remember { mutableStateOf<List<SupabaseCategory>>(emptyList()) }
    var repositories by remember { mutableStateOf<List<SupabaseRepo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val supabaseService = remember {
        Retrofit.Builder()
            .baseUrl(SupabaseClient.URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseService::class.java)
    }

    LaunchedEffect(Unit) {
        try {
            categories = supabaseService.getCategories(SupabaseClient.API_KEY, SupabaseClient.AUTH)
            repositories = supabaseService.getRepositories(SupabaseClient.API_KEY, SupabaseClient.AUTH)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(selectedRepo?.name ?: selectedCategory?.name ?: "Winlator Hub")
                },
                navigationIcon = {
                    if (selectedRepo != null) {
                        IconButton(onClick = { selectedRepo = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    } else if (selectedCategory != null) {
                        IconButton(onClick = { selectedCategory = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    if (isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (selectedCategory == null) {
                        CategoryList(categories) { cat ->
                            selectedCategory = cat
                        }
                    } else if (selectedRepo == null) {
                        val filteredRepos = repositories.filter { it.categoryId == selectedCategory!!.id }
                            .map { WinlatorRepo(id = it.name, name = it.name, owner = it.owner, repo = it.repo, description = it.description) }

                        RepoList(repos = filteredRepos) { repo ->
                            selectedRepo = repo
                        }
                    } else {
                        ReleaseList(repo = selectedRepo!!)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryList(categories: List<SupabaseCategory>, onCategoryClick: (SupabaseCategory) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            CategoryCard(category = category, onClick = { onCategoryClick(category) })
        }
    }
}

@Composable
fun CategoryCard(category: SupabaseCategory, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Category, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Text(category.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}

@Composable
fun RepoList(repos: List<WinlatorRepo>, onRepoClick: (WinlatorRepo) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(repos) { repo ->
            RepoCard(repo = repo, onClick = { onRepoClick(repo) })
        }
    }
}

@Composable
fun RepoCard(repo: WinlatorRepo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = repo.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = repo.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
fun ReleaseList(repo: WinlatorRepo) {
    var releases by remember { mutableStateOf<List<GitHubRelease>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val githubService = remember {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubService::class.java)
    }

    LaunchedEffect(repo) {
        isLoading = true
        try {
            releases = githubService.getReleases(repo.owner, repo.repo)
            isLoading = false
        } catch (e: Exception) {
            error = e.localizedMessage
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Erro: $error", color = MaterialTheme.colorScheme.error)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(releases ?: emptyList()) { release ->
                ReleaseCard(release = release)
            }
        }
    }
}

@Composable
fun ReleaseCard(release: GitHubRelease) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = release.name.ifEmpty { release.tagName },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Publicado em: ${release.publishedAt.substringBefore("T")}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = release.body,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Arquivos:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            release.assets.forEach { asset ->
                AssetItem(asset = asset) {
                    downloadFile(context, asset.downloadUrl, asset.name)
                }
            }
        }
    }
}

@Composable
fun AssetItem(asset: GitHubAsset, onDownload: () -> Unit) {
    val sizeMb = asset.size / (1024 * 1024)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = asset.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${sizeMb}MB",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Button(
            onClick = onDownload,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text("Baixar", fontSize = 12.sp)
        }
    }
}

fun downloadFile(context: Context, url: String, fileName: String) {
    try {
        val subPath = getDownloadPath(context)
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "$subPath/$fileName")

        val task = AppDownloadManager.addTask(url, file, fileName)
        task.start(AppDownloadManager.managerScope)

        val intent = Intent(context, com.winlator.downloader.DownloadService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        Toast.makeText(context, "Download iniciado em Downloads/$subPath", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao iniciar download: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
