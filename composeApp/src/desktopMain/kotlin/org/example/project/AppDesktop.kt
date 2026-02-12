package org.example.project

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import org.example.project.data.SettingsRepository
import org.example.project.data.desktop.DesktopSettingsRepository
import org.example.project.data.desktop.DesktopWeightRepository
import org.example.project.fitness.StepCountProvider

@Composable
fun AppDesktop() {
    val coroutineScope = rememberCoroutineScope()
    
    // Desktop-compatible implementations
    val weightRepository = remember { DesktopWeightRepository.create() }
    val settingsRepository = remember { DesktopSettingsRepository() as SettingsRepository }
    val stepCountProvider = remember { StepCountProvider() }
    
    App(
        weightRepository = weightRepository,
        settingsRepository = settingsRepository,
        stepCountProvider = stepCountProvider,
        onNotificationTimeChange = { _, _, _ ->
            // Desktop doesn't support notifications
        },
        onNotificationEnabledChange = { _, _, _, _ ->
            // Desktop doesn't support notifications
        },
        coroutineScope = coroutineScope
    )
}

