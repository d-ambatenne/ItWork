package org.example.project.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Adapter to make Room WeightDao compatible with WeightDaoInterface
// Converts between WeightEntryEntity (Room) and WeightEntry (common)
class WeightDaoAdapter(private val roomDao: org.example.project.data.WeightDao) : WeightDaoInterface {
    override fun getAllEntries(): Flow<List<WeightEntry>> = 
        roomDao.getAllEntries().map { entities -> entities.map { it.toCommon() } }
    
    override fun getAllEntriesAscending(): Flow<List<WeightEntry>> = 
        roomDao.getAllEntriesAscending().map { entities -> entities.map { it.toCommon() } }
    
    override suspend fun getEntryById(id: Long): WeightEntry? = 
        roomDao.getEntryById(id)?.toCommon()
    
    override suspend fun insertEntry(entry: WeightEntry): Long = 
        roomDao.insertEntry(WeightEntryEntity.fromCommon(entry))
    
    override suspend fun deleteEntry(entry: WeightEntry) = 
        roomDao.deleteEntry(WeightEntryEntity.fromCommon(entry))
    
    override suspend fun deleteEntryById(id: Long) = 
        roomDao.deleteEntryById(id)
}

