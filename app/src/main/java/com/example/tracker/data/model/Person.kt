package com.example.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String? = null,
    val phoneNumber: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
