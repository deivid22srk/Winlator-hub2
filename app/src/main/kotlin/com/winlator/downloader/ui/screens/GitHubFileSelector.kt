package com.winlator.downloader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.winlator.downloader.data.*
import kotlinx.coroutines.launch

data class GitHubSelection(
    val repo: SupabaseRepo? = null,
    val release: GitHubRelease? = null,
    val asset: GitHubAsset? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubFileSelector(
    label: String,
    categories: List<SupabaseCategory>,
    repositories: List<SupabaseRepo>,
    githubService: GitHubService,
    onSelectionChanged: (GitHubSelection) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<SupabaseCategory?>(null) }
    var selectedRepo by remember { mutableStateOf<SupabaseRepo?>(null) }
    var releases by remember { mutableStateOf<List<GitHubRelease>>(emptyList()) }
    var selectedRelease by remember { mutableStateOf<GitHubRelease?>(null) }
    var selectedAsset by remember { mutableStateOf<GitHubAsset?>(null) }

    var catExpanded by remember { mutableStateOf(false) }
    var repoExpanded by remember { mutableStateOf(false) }
    var releaseExpanded by remember { mutableStateOf(false) }
    var assetExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

        // Category Selector
        ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = !catExpanded }) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "Filtrar por Categoria",
                onValueChange = {}, readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                DropdownMenuItem(text = { Text("Todas") }, onClick = { selectedCategory = null; catExpanded = false })
                categories.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategory = cat; catExpanded = false })
                }
            }
        }

        // Repo Selector
        val filteredRepos = if (selectedCategory == null) repositories else repositories.filter { it.categoryId == selectedCategory!!.id }
        ExposedDropdownMenuBox(expanded = repoExpanded, onExpandedChange = { repoExpanded = !repoExpanded }) {
            OutlinedTextField(
                value = selectedRepo?.name ?: "Selecionar Repositório",
                onValueChange = {}, readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repoExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = repoExpanded, onDismissRequest = { repoExpanded = false }) {
                filteredRepos.forEach { repo ->
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
                            onSelectionChanged(GitHubSelection(repo, null, null))
                        }
                    )
                }
            }
        }

        // Release Selector
        if (selectedRepo != null) {
            ExposedDropdownMenuBox(expanded = releaseExpanded, onExpandedChange = { releaseExpanded = !releaseExpanded }) {
                OutlinedTextField(
                    value = selectedRelease?.tagName ?: "Selecionar Versão",
                    onValueChange = {}, readOnly = true,
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
                                onSelectionChanged(GitHubSelection(selectedRepo, release, null))
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
                    value = selectedAsset?.name ?: "Selecionar Arquivo",
                    onValueChange = {}, readOnly = true,
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
                                onSelectionChanged(GitHubSelection(selectedRepo, selectedRelease, asset))
                            }
                        )
                    }
                }
            }
        }
    }
}
