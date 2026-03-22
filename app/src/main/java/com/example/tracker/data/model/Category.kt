package com.example.tracker.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tracker.data.enums.CategoryType

@Entity(tableName = "categories", indices = [Index("type")])
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
    val color: String,
    val type: CategoryType,
    val isSystem: Boolean = false,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
