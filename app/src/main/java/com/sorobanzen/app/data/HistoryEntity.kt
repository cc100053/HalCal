package com.sorobanzen.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val expression: String,
    val result: String,
    val mode: String, // "Normal" or "Soroban"
    val timestamp: Long = System.currentTimeMillis()
)
