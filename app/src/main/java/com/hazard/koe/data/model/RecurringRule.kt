package com.hazard.koe.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hazard.koe.data.enums.FrequencyType
import com.hazard.koe.data.enums.RecurringType

@Entity(
    tableName = "recurring_rules",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = SubscriptionService::class,
            parentColumns = ["id"],
            childColumns = ["subscriptionServiceId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("accountId"), Index("categoryId"), Index("subscriptionServiceId")]
)
data class RecurringRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: RecurringType,
    val amount: Long,
    val accountId: Long,
    val categoryId: Long,
    val subscriptionServiceId: Long? = null,
    val frequencyType: FrequencyType,
    val frequencyInterval: Int = 1,
    val dayOfMonth: Int? = null,
    val dayOfWeek: Int? = null,
    val startDate: Long,
    val endDate: Long? = null,
    val nextOccurrence: Long,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
