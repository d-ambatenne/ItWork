package org.example.project.data

import kotlinx.coroutines.flow.Flow

// Common interface for SettingsRepository
interface SettingsRepository {
    val notificationEnabled: Flow<Boolean>
    val notificationHour: Flow<Int>
    val notificationMinute: Flow<Int>
    val notificationDays: Flow<Set<Int>> // Set of Calendar.DAY_OF_WEEK values (1=Sunday, 2=Monday, etc.)
    
    suspend fun setNotificationEnabled(enabled: Boolean)
    suspend fun setNotificationTime(hour: Int, minute: Int)
    suspend fun setNotificationDays(days: Set<Int>)
    
    // Water intake persistence
    suspend fun getWaterIntakeCount(): Int
    suspend fun getWaterIntakeDate(): Long // Date when water intake was last updated (timestamp at 00:00 of that day)
    suspend fun setWaterIntake(count: Int, date: Long)
}

