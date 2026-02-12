package org.example.project.data.desktop

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.project.data.WeightEntry
import org.example.project.data.WeightDaoInterface
import org.example.project.data.WeightRepository

// Desktop-compatible WeightDao implementation
class DesktopWeightDao : WeightDaoInterface {
    private val entries = MutableStateFlow<List<WeightEntry>>(emptyList())
    private var nextId = 1L
    
    override fun getAllEntries(): Flow<List<WeightEntry>> = entries
    
    override fun getAllEntriesAscending(): Flow<List<WeightEntry>> = 
        entries.map { it.sortedBy { entry -> entry.date } }
    
    override suspend fun getEntryById(id: Long): WeightEntry? = 
        entries.value.find { it.id == id }
    
    override suspend fun insertEntry(entry: WeightEntry): Long {
        val newEntry = entry.copy(id = nextId++)
        entries.value = entries.value + newEntry
        return newEntry.id
    }
    
    override suspend fun deleteEntry(entry: WeightEntry) {
        entries.value = entries.value.filter { it.id != entry.id }
    }
    
    override suspend fun deleteEntryById(id: Long) {
        entries.value = entries.value.filter { it.id != id }
    }
}

// Desktop-compatible WeightRepository factory
object DesktopWeightRepository {
    fun create(): WeightRepository {
        return WeightRepository(DesktopWeightDao())
    }
}

