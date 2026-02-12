package org.example.project.ui.settings

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.project.data.SettingsRepository

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope
) {
    
    val notificationEnabled: StateFlow<Boolean> = settingsRepository.notificationEnabled
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    val notificationHour: StateFlow<Int> = settingsRepository.notificationHour
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 8
        )
    
    val notificationMinute: StateFlow<Int> = settingsRepository.notificationMinute
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    
    val notificationDays: StateFlow<Set<Int>> = settingsRepository.notificationDays
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = setOf(2, 3, 4, 5, 6, 7, 1) // Default: all days
        )
    
    fun setNotificationEnabled(enabled: Boolean) {
        scope.launch {
            settingsRepository.setNotificationEnabled(enabled)
        }
    }
    
    fun setNotificationTime(hour: Int, minute: Int) {
        scope.launch {
            settingsRepository.setNotificationTime(hour, minute)
        }
    }
    
    fun setNotificationDays(days: Set<Int>) {
        scope.launch {
            settingsRepository.setNotificationDays(days)
        }
    }
}

