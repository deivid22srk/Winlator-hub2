package com.winlator.downloader.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : NavigationItem("home", Icons.Default.Home, "Início")
    object Downloads : NavigationItem("downloads", Icons.Default.Download, "Downloads")
    object Settings : NavigationItem("settings", Icons.Default.Settings, "Ajustes")
}
