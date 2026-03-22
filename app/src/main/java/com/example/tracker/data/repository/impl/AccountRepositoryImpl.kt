package com.example.tracker.data.repository.impl

import com.example.tracker.data.db.dao.AccountDao
import com.example.tracker.data.enums.AccountType
import com.example.tracker.data.model.Account
import com.example.tracker.data.model.relations.CurrencyBalance
import com.example.tracker.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class AccountRepositoryImpl(private val dao: AccountDao) : AccountRepository {

    override fun getAll(): Flow<List<Account>> = dao.getAll()

    override fun getById(id: Long): Flow<Account?> = dao.getById(id)

    override fun getByType(type: AccountType): Flow<List<Account>> = dao.getByType(type)

    override fun getTotalBalanceByCurrency(): Flow<List<CurrencyBalance>> = dao.getTotalBalanceByCurrency()

    override suspend fun create(account: Account): Long = dao.insert(account)

    override suspend fun update(account: Account) = dao.update(account)

    override suspend fun updateBalance(id: Long, newBalance: Long) = dao.updateBalance(id, newBalance)

    override suspend fun updateCreditUsed(id: Long, creditUsed: Long) = dao.updateCreditUsed(id, creditUsed)

    override suspend fun archive(id: Long) = dao.archive(id)
}
