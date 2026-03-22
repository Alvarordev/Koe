package com.example.tracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.tracker.data.enums.LoanDirection

@Entity(
    tableName = "casual_loans",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("personId")]
)
data class CasualLoan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val direction: LoanDirection,
    val originalAmount: Long,
    val outstandingBalance: Long,
    val currencyCode: String,
    val description: String? = null,
    val dueDate: Long? = null,
    val isPaidOff: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
