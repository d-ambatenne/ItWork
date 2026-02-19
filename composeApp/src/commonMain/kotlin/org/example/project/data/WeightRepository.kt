package org.example.project.data

import kotlinx.coroutines.flow.Flow

class WeightRepository(private val weightDao: WeightDaoInterface) {
    fun getAllEntries(): Flow<List<WeightEntry>> = weightDao.getAllEntries()
    
    fun getAllEntriesAscending(): Flow<List<WeightEntry>> = weightDao.getAllEntriesAscending()
    
    suspend fun getEntryById(id: Long): WeightEntry? = weightDao.getEntryById(id)
    
    suspend fun insertEntry(entry: WeightEntry): Long = weightDao.insertEntry(entry)
    
    suspend fun deleteEntry(entry: WeightEntry) = weightDao.deleteEntry(entry)
    
    suspend fun deleteEntryById(id: Long) = weightDao.deleteEntryById(id)

    suspend fun bulkImport(entries: List<WeightEntry>): Int {
        var count = 0
        for (entry in entries) {
            try {
                if (entry.weight > 0 && entry.weight < 500) {
                    weightDao.insertEntry(entry)
                    count++
                    if (count % 100 == 0) {
                        // TODO: add progress callback
                    }
                }
            } catch (e: Exception) {
            }
        }
        return count
    }
}
