package org.example.project.data

import kotlinx.coroutines.flow.Flow

class WeightRepository(private val weightDao: WeightDaoInterface) {
    fun getAllEntries(): Flow<List<WeightEntry>> = weightDao.getAllEntries()
    
    fun getAllEntriesAscending(): Flow<List<WeightEntry>> = weightDao.getAllEntriesAscending()
    
    suspend fun getEntryById(id: Long): WeightEntry? = weightDao.getEntryById(id)
    
    suspend fun insertEntry(entry: WeightEntry): Long = weightDao.insertEntry(entry)
    
    suspend fun deleteEntry(entry: WeightEntry) = weightDao.deleteEntry(entry)
    
    suspend fun deleteEntryById(id: Long) = weightDao.deleteEntryById(id)
}


