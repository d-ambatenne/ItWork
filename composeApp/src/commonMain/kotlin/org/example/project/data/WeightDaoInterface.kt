package org.example.project.data

import kotlinx.coroutines.flow.Flow

// Common interface for WeightDao that works on all platforms
interface WeightDaoInterface {
    fun getAllEntries(): Flow<List<WeightEntry>>
    fun getAllEntriesAscending(): Flow<List<WeightEntry>>
    suspend fun getEntryById(id: Long): WeightEntry?
    suspend fun insertEntry(entry: WeightEntry): Long
    suspend fun deleteEntry(entry: WeightEntry)
    suspend fun deleteEntryById(id: Long)
}


