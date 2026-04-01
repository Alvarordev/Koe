package com.hazard.koe.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hazard.koe.data.enums.PaymentStatus

@Entity(
    tableName = "formal_loan_payments",
    foreignKeys = [
        ForeignKey(
            entity = FormalLoan::class,
            parentColumns = ["id"],
            childColumns = ["formalLoanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("formalLoanId"), Index("status"), Index("transactionId")]
)
data class FormalLoanPayment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val formalLoanId: Long,
    val transactionId: Long? = null,
    val paymentNumber: Int,
    val dueDate: Long,
    val principalPortion: Long,
    val interestPortion: Long,
    val totalAmount: Long,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val paidDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
