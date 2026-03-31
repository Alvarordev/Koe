package com.example.tracker.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class UserSubscription(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val serviceId: Long? = null,
    @ColumnInfo(index = true) val accountId: Long,
    val amount: Long,
    val billingDay: Int,
    val currencyCode: String,
    val customName: String? = null,
    val customEmoji: String? = null,
    val iconResName: String? = null,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
