package org.example.project.fitness

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Desktop implementation that always returns null (not supported on desktop).
 */
actual class StepCountProvider {
    actual val dailyStepCount: Flow<Int?> = flowOf(null)
    
    actual suspend fun requestPermission(): Boolean = false
    
    actual suspend fun hasPermission(): Boolean = false
    
    actual suspend fun refresh() {
        // No-op on desktop
    }
}




























