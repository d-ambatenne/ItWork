package org.example.project.fitness

import kotlinx.coroutines.flow.Flow

/**
 * Interface for providing step count data.
 * Implementations should provide daily step count from fitness tracking services.
 */
expect class StepCountProvider {
    /**
     * Flow of current day's step count.
     * Emits the step count whenever it changes.
     * Returns null if step count is not available.
     */
    val dailyStepCount: Flow<Int?>
    
    /**
     * Request permission to access fitness data.
     * Should be called before accessing step count.
     */
    suspend fun requestPermission(): Boolean
    
    /**
     * Check if permission is granted.
     */
    suspend fun hasPermission(): Boolean
    
    /**
     * Refresh the step count data.
     */
    suspend fun refresh()
}




























