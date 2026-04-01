package com.hazard.koe.domain.repository

import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.relations.CurrencyBalance
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAll(): Flow<List<Account>>
    fun getById(id: Long): Flow<Account?>
    fun getByType(type: AccountType): Flow<List<Account>>
    fun getTotalBalance(): Flow<Long>
    fun getTotalBalanceByCurrency(): Flow<List<CurrencyBalance>>
    suspend fun create(account: Account): Long
    suspend fun update(account: Account)
    suspend fun updateBalance(id: Long, newBalance: Long)
    suspend fun updateCreditUsed(id: Long, creditUsed: Long)
    suspend fun archive(id: Long)
}
