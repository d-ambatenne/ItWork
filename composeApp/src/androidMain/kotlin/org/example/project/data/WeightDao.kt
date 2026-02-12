package org.example.project.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<WeightEntryEntity>>
    
    @Query("SELECT * FROM weight_entries ORDER BY date ASC")
    fun getAllEntriesAscending(): Flow<List<WeightEntryEntity>>
    
    @Query("SELECT * FROM weight_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): WeightEntryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: WeightEntryEntity): Long
    
    @Delete
    suspend fun deleteEntry(entry: WeightEntryEntity)
    
    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)
}

