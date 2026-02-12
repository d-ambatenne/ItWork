package org.example.project.ui.theme

import androidx.compose.runtime.Composable

@Composable
actual fun isSystemInDarkTheme(): Boolean {
    // For desktop, default to light theme
    // Could be enhanced to detect system theme if needed
    return false
}


