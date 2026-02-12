package org.example.project.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AndroidSettingsRepository(private val context: Context) : SettingsRepository {
    companion object {
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
        val NOTIFICATION_DAYS = stringSetPreferencesKey("notification_days")
        val WATER_INTAKE_COUNT = intPreferencesKey("water_intake_count")
        val WATER_INTAKE_DATE = longPreferencesKey("water_intake_date")
    }
    
    override val notificationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_ENABLED] ?: false
    }
    
    override val notificationHour: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_HOUR] ?: 8
    }
    
    override val notificationMinute: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_MINUTE] ?: 0
    }
    
    override val notificationDays: Flow<Set<Int>> = context.dataStore.data.map { preferences ->
        val dayStrings = preferences[NOTIFICATION_DAYS] ?: setOf("2", "3", "4", "5", "6", "7", "1") // Default: all days (Mon-Sun)
        dayStrings.map { it.toInt() }.toSet()
    }
    
    override suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED] = enabled
        }
    }
    
    override suspend fun setNotificationTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_HOUR] = hour
            preferences[NOTIFICATION_MINUTE] = minute
        }
    }
    
    override suspend fun setNotificationDays(days: Set<Int>) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_DAYS] = days.map { it.toString() }.toSet()
        }
    }
    
    override suspend fun getWaterIntakeCount(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[WATER_INTAKE_COUNT] ?: 0
        }.first()
    }
    
    override suspend fun getWaterIntakeDate(): Long {
        return context.dataStore.data.map { preferences ->
            preferences[WATER_INTAKE_DATE] ?: 0L
        }.first()
    }
    
    override suspend fun setWaterIntake(count: Int, date: Long) {
        context.dataStore.edit { preferences ->
            preferences[WATER_INTAKE_COUNT] = count
            preferences[WATER_INTAKE_DATE] = date
        }
    }
}

