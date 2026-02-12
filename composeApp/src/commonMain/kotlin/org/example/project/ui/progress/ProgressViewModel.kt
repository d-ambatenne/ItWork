package org.example.project.ui.progress

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.project.data.WeightEntry
import org.example.project.data.WeightRepository
import org.example.project.data.SettingsRepository
import java.util.Calendar

class ProgressViewModel(
    private val repository: WeightRepository,
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope
) {
    
    val weightEntries: StateFlow<List<WeightEntry>> = repository.getAllEntries()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Water tracking state
    private val _waterCupsCount = MutableStateFlow(0)
    val waterCupsCount: StateFlow<Int> = _waterCupsCount
    
    // Track which cup indices are currently animating
    private val _animatingCups = MutableStateFlow<Set<Int>>(emptySet())
    val animatingCups: StateFlow<Set<Int>> = _animatingCups
    
    // Track if a new cup was just added (for animation/UI feedback)
    private val _newCupAdded = MutableStateFlow(false)
    val newCupAdded: StateFlow<Boolean> = _newCupAdded
    
    init {
        // Initialize water intake: check if date has changed and reset if needed
        scope.launch {
            val currentDayStart = getStartOfDay(System.currentTimeMillis())
            val storedDate = settingsRepository.getWaterIntakeDate()
            
            if (storedDate == 0L || storedDate < currentDayStart) {
                // New day or first time - reset to 0
                _waterCupsCount.value = 0
                settingsRepository.setWaterIntake(0, currentDayStart)
            } else {
                // Same day - load stored count
                val storedCount = settingsRepository.getWaterIntakeCount()
                _waterCupsCount.value = storedCount
            }
        }
    }
    
    /**
     * Get the timestamp for the start of the day (00:00:00) for a given timestamp
     */
    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
    
    /**
     * Check if date has changed and reset if needed. Returns true if reset occurred.
     * This should be called before modifying water intake count.
     */
    private suspend fun checkAndResetIfNewDay(): Boolean {
        val currentDayStart = getStartOfDay(System.currentTimeMillis())
        val storedDate = settingsRepository.getWaterIntakeDate()
        
        if (storedDate < currentDayStart) {
            // New day - reset to 0
            _waterCupsCount.value = 0
            settingsRepository.setWaterIntake(0, currentDayStart)
            return true
        }
        return false
    }
    
    fun deleteEntry(entry: WeightEntry) {
        scope.launch {
            repository.deleteEntry(entry)
        }
    }
    
    fun getLatestWeight(): Float? {
        return weightEntries.value.maxByOrNull { it.date }?.weight
    }
    
    fun getWeightChange(): Float? {
        val entries = weightEntries.value.sortedBy { it.date }
        if (entries.size < 2) return null
        val first = entries.first().weight
        val last = entries.last().weight
        return last - first
    }
    
    fun saveWeight(weight: Float, date: Long = System.currentTimeMillis(), note: String? = null) {
        scope.launch {
            val entry = WeightEntry(
                weight = weight,
                date = date,
                note = note
            )
            repository.insertEntry(entry)
        }
    }
    
    fun addWaterCup() {
        scope.launch {
            // Check if date has changed and reset if needed
            checkAndResetIfNewDay()
            
        val newCupIndex = _waterCupsCount.value
        _waterCupsCount.value += 1
        // Add the new cup index to animating set
        _animatingCups.value = _animatingCups.value + newCupIndex
        
            // Set newCupAdded to true to trigger UI feedback
            _newCupAdded.value = true
            
            // Persist the change
            val currentDayStart = getStartOfDay(System.currentTimeMillis())
            settingsRepository.setWaterIntake(_waterCupsCount.value, currentDayStart)
            
            // Remove from animating set after animation completes
            // Animation duration is 1000ms, so wait a bit longer to ensure it completes
            kotlinx.coroutines.delay(1100)
            _animatingCups.value = _animatingCups.value - newCupIndex
            // Reset newCupAdded flag after animation
            _newCupAdded.value = false
        }
    }
    
    fun removeWaterCup() {
        scope.launch {
            // Check if date has changed and reset if needed
            if (checkAndResetIfNewDay()) {
                return@launch // Already reset, nothing to remove
            }
            
        if (_waterCupsCount.value > 0) {
            val removedIndex = _waterCupsCount.value - 1
            _waterCupsCount.value -= 1
            // Remove the deleted cup from animating set and adjust indices of cups after it
            _animatingCups.value = _animatingCups.value
                .filter { it != removedIndex } // Remove the deleted cup
                .map { if (it > removedIndex) it - 1 else it } // Shift down indices after removed cup
                .toSet()
                
                // Persist the change
                val currentDayStart = getStartOfDay(System.currentTimeMillis())
                settingsRepository.setWaterIntake(_waterCupsCount.value, currentDayStart)
            }
        }
    }
    
    fun resetWaterCups() {
        scope.launch {
        _waterCupsCount.value = 0
            val currentDayStart = getStartOfDay(System.currentTimeMillis())
            settingsRepository.setWaterIntake(0, currentDayStart)
        }
    }
}

