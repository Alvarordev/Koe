package com.hazard.koe.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.enums.CardNetwork

@Entity(tableName = "accounts", indices = [Index("type")])
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val color: String,
    val currencyCode: String,
    val initialBalance: Long,
    val currentBalance: Long,
    val cardNetwork: CardNetwork? = null,
    val lastFourDigits: String? = null,
    val expirationDate: String? = null,
    val creditLimit: Long? = null,
    val creditUsed: Long? = null,
    val paymentDay: Int? = null,
    val closingDay: Int? = null,
    val interestRate: Double? = null,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
