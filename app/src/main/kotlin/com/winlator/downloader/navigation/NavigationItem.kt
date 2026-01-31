package com.winlator.downloader.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : NavigationItem("home", Icons.Default.Home, "Início")
    object Downloads : NavigationItem("downloads", Icons.Default.Download, "Downloads")
    object GameSettings : NavigationItem("games", Icons.Default.VideogameAsset, "Jogos")
    object AddGame : NavigationItem("add_game", Icons.Default.Add, "Adicionar Jogo")
    object GameDetails : NavigationItem("game_details", Icons.Default.Description, "Detalhes")
    object Settings : NavigationItem("settings", Icons.Default.Settings, "Ajustes")
}
