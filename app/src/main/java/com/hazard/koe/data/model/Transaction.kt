package com.hazard.koe.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hazard.koe.data.enums.TransactionType

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["transferToAccountId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = RecurringRule::class,
            parentColumns = ["id"],
            childColumns = ["recurringRuleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("accountId"),
        Index("categoryId"),
        Index("date"),
        Index("type"),
        Index("subscriptionId"),
        Index("transferToAccountId"),
        Index("recurringRuleId")
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amount: Long,
    val description: String? = null,
    val accountId: Long,
    val transferToAccountId: Long? = null,
    val categoryId: Long,
    val subscriptionId: Long? = null,
    val exchangeRate: Double? = null,
    val convertedAmount: Long? = null,
    val date: Long,
    val isRecurring: Boolean = false,
    val recurringRuleId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null
)
