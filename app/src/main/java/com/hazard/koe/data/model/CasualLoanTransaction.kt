package com.hazard.koe.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hazard.koe.data.enums.CasualLoanTxnType

@Entity(
    tableName = "casual_loan_transactions",
    foreignKeys = [
        ForeignKey(
            entity = CasualLoan::class,
            parentColumns = ["id"],
            childColumns = ["casualLoanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("casualLoanId"), Index("transactionId")]
)
data class CasualLoanTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val casualLoanId: Long,
    val transactionId: Long,
    val amount: Long,
    val type: CasualLoanTxnType,
    val note: String? = null,
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)
