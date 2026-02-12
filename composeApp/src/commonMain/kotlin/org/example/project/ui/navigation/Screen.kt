package org.example.project.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Progress : Screen("progress", "Progress")
    object Settings : Screen("settings", "Settings")
}


