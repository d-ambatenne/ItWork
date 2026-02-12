package org.example.project.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Android Room entity - this is what Room processes
@Entity(tableName = "weight_entries")
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weight: Float,
    val date: Long = System.currentTimeMillis(),
    val note: String? = null
) {
    fun toCommon(): WeightEntry = WeightEntry(id, weight, date, note)
    companion object {
        fun fromCommon(entry: WeightEntry): WeightEntryEntity = 
            WeightEntryEntity(entry.id, entry.weight, entry.date, entry.note)
    }
}

