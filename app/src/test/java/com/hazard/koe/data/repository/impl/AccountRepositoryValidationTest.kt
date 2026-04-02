package com.hazard.koe.data.repository.impl

import com.hazard.koe.data.db.dao.AccountDao
import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.model.Account
import com.hazard.koe.domain.exception.InvalidAccountConfigurationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountRepositoryValidationTest {

    @Test
    fun createCreditAccount_throwsWhenCreditUsedExceedsLimit() = runTest {
        val dao = FakeAccountDao()
        val repository = AccountRepositoryImpl(dao)
        val account = Account(
            name = "Credit",
            type = AccountType.CREDIT,
            color = "#000000",
            currencyCode = "USD",
            initialBalance = 0L,
            currentBalance = 0L,
            creditLimit = 100_00L,
            creditUsed = 150_00L,
            paymentDay = 10
        )

        val result = runCatching { repository.create(account) }

        assertTrue(result.exceptionOrNull() is InvalidAccountConfigurationException)
    }

    @Test
    fun createCreditAccount_allowsUsedEqualToLimit() = runTest {
        val dao = FakeAccountDao()
        val repository = AccountRepositoryImpl(dao)
        val account = Account(
            name = "Credit",
            type = AccountType.CREDIT,
            color = "#000000",
            currencyCode = "USD",
            initialBalance = 0L,
            currentBalance = 0L,
            creditLimit = 100_00L,
            creditUsed = 100_00L,
            paymentDay = 10
        )

        repository.create(account)

        assertEquals(1, dao.insertCount)
    }

    private class FakeAccountDao : AccountDao {
        var insertCount: Int = 0

        override fun getAll(): Flow<List<Account>> = emptyFlow()
        override fun getById(id: Long): Flow<Account?> = emptyFlow()
        override fun getByType(type: AccountType): Flow<List<Account>> = emptyFlow()
        override suspend fun insert(account: Account): Long {
            insertCount++
            return 1L
        }
        override suspend fun update(account: Account) = Unit
        override suspend fun updateBalance(id: Long, newBalance: Long, now: Long) = Unit
        override suspend fun updateCreditUsed(id: Long, creditUsed: Long, now: Long) = Unit
        override suspend fun archive(id: Long, now: Long) = Unit
        override fun getTotalBalance(): Flow<Long> = emptyFlow()
        override fun getTotalBalanceByCurrency(): Flow<List<com.hazard.koe.data.model.relations.CurrencyBalance>> = emptyFlow()
    }
}
