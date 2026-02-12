package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import org.example.project.data.SettingsRepository
import org.example.project.data.WeightDaoInterface
import org.example.project.data.WeightEntry
import org.example.project.data.WeightRepository
import org.example.project.ui.navigation.Screen
import org.example.project.ui.navigation.rememberSimpleNavController
import org.example.project.ui.progress.ProgressScreen
import org.example.project.ui.progress.ProgressViewModel
import org.example.project.ui.settings.SettingsScreen
import org.example.project.ui.settings.SettingsViewModel
import org.example.project.ui.theme.GreenishDarkColorScheme
import org.example.project.ui.theme.GreenishLightColorScheme
import org.example.project.ui.theme.isSystemInDarkTheme
import org.example.project.fitness.StepCountProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    weightRepository: WeightRepository,
    settingsRepository: SettingsRepository,
    stepCountProvider: StepCountProvider? = null,
    onRequestGoogleFitPermission: (() -> Unit)? = null,
    onNotificationTimeChange: (Int, Int, Set<Int>) -> Unit = { _, _, _ -> },
    onNotificationEnabledChange: (Boolean, Int, Int, Set<Int>) -> Unit = { _, _, _, _ -> },
    onTestNotification: (() -> Unit)? = null,
    onRequestBatteryOptimization: (() -> Unit)? = null,
    onRequestExactAlarm: (() -> Unit)? = null,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    val navController = rememberSimpleNavController()
    val currentRoute = navController.currentRouteValue
    val isDarkTheme = isSystemInDarkTheme()
    val colorScheme = if (isDarkTheme) GreenishDarkColorScheme else GreenishLightColorScheme
    
    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(
                            Screen.Progress to Icons.Default.TrendingUp,
                            Screen.Settings to Icons.Default.Settings
                        ).forEach { (screen, icon) ->
                            val isSelected = currentRoute == screen.route
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { navController.navigate(screen.route) }
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(bottom = 4.dp)
                                        .then(
                                            if (isSelected) {
                                                Modifier.background(
                                                    MaterialTheme.colorScheme.primary,
                                                    shape = CircleShape
                                                ).padding(8.dp)
                                            } else {
                                                Modifier.padding(8.dp)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = screen.title,
                                        tint = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            when (currentRoute) {
                Screen.Progress.route -> {
                    val viewModel = remember {
                        ProgressViewModel(weightRepository, settingsRepository, coroutineScope)
                    }
                    ProgressScreen(
                        repository = weightRepository,
                        viewModel = viewModel,
                        stepCountProvider = stepCountProvider,
                        onRequestGoogleFitPermission = onRequestGoogleFitPermission,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                Screen.Settings.route -> {
                    val viewModel = remember {
                        SettingsViewModel(settingsRepository, coroutineScope)
                    }
                    SettingsScreen(
                        settingsRepository = settingsRepository,
                        viewModel = viewModel,
                        onNotificationTimeChange = { hour, minute, days ->
                            onNotificationTimeChange(hour, minute, days)
                        },
                        onNotificationEnabledChange = { enabled, hour, minute, days ->
                            onNotificationEnabledChange(enabled, hour, minute, days)
                        },
                        onTestNotification = onTestNotification,
                        onRequestBatteryOptimization = onRequestBatteryOptimization,
                        onRequestExactAlarm = onRequestExactAlarm,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

