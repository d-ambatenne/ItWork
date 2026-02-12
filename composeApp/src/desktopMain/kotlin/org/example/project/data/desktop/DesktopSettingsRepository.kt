package org.example.project.data.desktop

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.example.project.data.SettingsRepository

class DesktopSettingsRepository : SettingsRepository {
    private val notificationEnabledFlow = MutableStateFlow(false)
    private val notificationHourFlow = MutableStateFlow(8)
    private val notificationMinuteFlow = MutableStateFlow(0)
    private val notificationDaysFlow = MutableStateFlow(setOf(2, 3, 4, 5, 6, 7, 1)) // Default: all days
    private val waterIntakeCountFlow = MutableStateFlow(0)
    private val waterIntakeDateFlow = MutableStateFlow(0L)
    
    override val notificationEnabled: Flow<Boolean> = notificationEnabledFlow
    override val notificationHour: Flow<Int> = notificationHourFlow
    override val notificationMinute: Flow<Int> = notificationMinuteFlow
    override val notificationDays: Flow<Set<Int>> = notificationDaysFlow
    
    override suspend fun setNotificationEnabled(enabled: Boolean) {
        notificationEnabledFlow.value = enabled
    }
    
    override suspend fun setNotificationTime(hour: Int, minute: Int) {
        notificationHourFlow.value = hour
        notificationMinuteFlow.value = minute
    }
    
    override suspend fun setNotificationDays(days: Set<Int>) {
        notificationDaysFlow.value = days
    }
    
    override suspend fun getWaterIntakeCount(): Int {
        return waterIntakeCountFlow.value
    }
    
    override suspend fun getWaterIntakeDate(): Long {
        return waterIntakeDateFlow.value
    }
    
    override suspend fun setWaterIntake(count: Int, date: Long) {
        waterIntakeCountFlow.value = count
        waterIntakeDateFlow.value = date
    }
}

