package org.example.project.data

// Common WeightEntry data class
// Android will use this with Room annotations via typealias or Room will process it
data class WeightEntry(
    val id: Long = 0,
    val weight: Float,
    val date: Long = System.currentTimeMillis(),
    val note: String? = null
)


