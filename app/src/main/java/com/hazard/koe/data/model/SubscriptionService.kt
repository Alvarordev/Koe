package com.hazard.koe.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscription_services")
data class SubscriptionService(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val name: String,
    val iconResName: String,
    val color: String,
    val isSystem: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
