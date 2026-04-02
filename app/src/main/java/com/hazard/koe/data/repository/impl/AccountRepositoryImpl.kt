package com.hazard.koe.data.repository.impl

import com.hazard.koe.data.db.dao.AccountDao
import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.relations.CurrencyBalance
import com.hazard.koe.domain.exception.InvalidAccountConfigurationException
import com.hazard.koe.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class AccountRepositoryImpl(private val dao: AccountDao) : AccountRepository {

    override fun getAll(): Flow<List<Account>> = dao.getAll()

    override fun getById(id: Long): Flow<Account?> = dao.getById(id)

    override fun getByType(type: AccountType): Flow<List<Account>> = dao.getByType(type)

    override fun getTotalBalance(): Flow<Long> = dao.getTotalBalance()

    override fun getTotalBalanceByCurrency(): Flow<List<CurrencyBalance>> = dao.getTotalBalanceByCurrency()

    override suspend fun create(account: Account): Long {
        validateAccount(account)
        return dao.insert(account)
    }

    override suspend fun update(account: Account) {
        validateAccount(account)
        dao.update(account)
    }

    override suspend fun updateBalance(id: Long, newBalance: Long) = dao.updateBalance(id, newBalance)

    override suspend fun updateCreditUsed(id: Long, creditUsed: Long) = dao.updateCreditUsed(id, creditUsed)

    override suspend fun archive(id: Long) = dao.archive(id)

    private fun validateAccount(account: Account) {
        account.paymentDay?.let {
            if (it !in 1..31) {
                throw InvalidAccountConfigurationException("Payment day must be between 1 and 31")
            }
        }
        account.closingDay?.let {
            if (it !in 1..31) {
                throw InvalidAccountConfigurationException("Closing day must be between 1 and 31")
            }
        }

        if (account.type == AccountType.CREDIT) {
            val creditLimit = account.creditLimit ?: 0L
            val creditUsed = account.creditUsed ?: 0L
            if (creditUsed < 0L) {
                throw InvalidAccountConfigurationException("Credit used cannot be negative")
            }
            if (creditUsed > creditLimit) {
                throw InvalidAccountConfigurationException("Credit used cannot exceed credit limit")
            }
        }
    }
}
