package com.hazard.koe.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "formal_loans",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("accountId")]
)
data class FormalLoan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val lenderName: String,
    val principalAmount: Long,
    val outstandingBalance: Long,
    val currencyCode: String,
    val annualRate: Double,
    val monthlyRate: Double,
    val termMonths: Int,
    val monthlyPayment: Long,
    val accountId: Long,
    val startDate: Long,
    val paymentDayOfMonth: Int,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
