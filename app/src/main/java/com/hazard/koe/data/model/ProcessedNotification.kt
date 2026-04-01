package com.hazard.koe.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "processed_notifications",
    indices = [Index("dedupKey", unique = true), Index("operationNumber")]
)
data class ProcessedNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dedupKey: String,
    val operationNumber: String? = null,
    val amount: Long,
    val type: String,
    val processedAt: Long = System.currentTimeMillis()
)
