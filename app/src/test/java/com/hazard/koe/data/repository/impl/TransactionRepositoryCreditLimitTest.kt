package com.hazard.koe.data.repository.impl

import com.hazard.koe.data.db.dao.AccountDao
import com.hazard.koe.data.db.dao.TransactionDao
import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.Transaction
import com.hazard.koe.data.model.relations.CategoryIdSummary
import com.hazard.koe.data.model.relations.CategorySummary
import com.hazard.koe.data.model.relations.CategoryTotal
import com.hazard.koe.data.model.relations.CurrencyBalance
import com.hazard.koe.data.model.relations.TransactionWithDetails
import com.hazard.koe.data.model.relations.TransactionWithMapData
import com.hazard.koe.domain.exception.CreditLimitExceededException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionRepositoryCreditLimitTest {

    @Test
    fun createExpenseForCredit_throwsWhenExceedsLimit() = runTest {
        val accountDao = FakeAccountDao(
            account = Account(
                id = 10L,
                name = "Credit",
                type = AccountType.CREDIT,
                color = "#000000",
                currencyCode = "USD",
                initialBalance = 0L,
                currentBalance = 0L,
                creditLimit = 100_00L,
                creditUsed = 90_00L,
                paymentDay = 10
            )
        )
        val transactionDao = FakeTransactionDao()
        val repository = TransactionRepositoryImpl(transactionDao, accountDao)

        val tx = Transaction(
            type = TransactionType.EXPENSE,
            amount = 20_00L,
            accountId = 10L,
            categoryId = 1L,
            date = System.currentTimeMillis()
        )

        val result = runCatching { repository.create(tx) }

        assertTrue(result.exceptionOrNull() is CreditLimitExceededException)
        assertEquals(0, transactionDao.insertCalls)
    }

    private class FakeAccountDao(private val account: Account?) : AccountDao {
        override fun getAll(): Flow<List<Account>> = flowOf(emptyList())
        override fun getById(id: Long): Flow<Account?> = flowOf(account)
        override fun getByType(type: AccountType): Flow<List<Account>> = flowOf(emptyList())
        override suspend fun insert(account: Account): Long = 0L
        override suspend fun update(account: Account) = Unit
        override suspend fun updateBalance(id: Long, newBalance: Long, now: Long) = Unit
        override suspend fun updateCreditUsed(id: Long, creditUsed: Long, now: Long) = Unit
        override suspend fun archive(id: Long, now: Long) = Unit
        override fun getTotalBalance(): Flow<Long> = flowOf(0L)
        override fun getTotalBalanceByCurrency(): Flow<List<CurrencyBalance>> = flowOf(emptyList())
    }

    private class FakeTransactionDao : TransactionDao {
        var insertCalls = 0

        override fun getAll(): Flow<List<TransactionWithDetails>> = flowOf(emptyList())
        override fun getById(id: Long): Flow<TransactionWithDetails?> = flowOf(null)
        override fun getByAccount(accountId: Long): Flow<List<TransactionWithDetails>> = flowOf(emptyList())
        override fun getByCategory(categoryId: Long): Flow<List<TransactionWithDetails>> = flowOf(emptyList())
        override fun getByDateRange(start: Long, end: Long): Flow<List<TransactionWithDetails>> = flowOf(emptyList())
        override fun getByType(type: TransactionType): Flow<List<TransactionWithDetails>> = flowOf(emptyList())
        override fun getExpensesByCategoryInPeriod(start: Long, end: Long): Flow<List<CategoryTotal>> = flowOf(emptyList())
        override fun getCategorySummaryInPeriod(categoryId: Long, start: Long, end: Long): Flow<CategorySummary> = flowOf(CategorySummary(0, 0L))
        override fun getAllCategorySummariesInPeriod(start: Long, end: Long): Flow<List<CategoryIdSummary>> = flowOf(emptyList())
        override fun getTotalByTypeInPeriod(type: TransactionType, start: Long, end: Long): Flow<Long> = flowOf(0L)
        override suspend fun getLastBySubscriptionId(subscriptionId: Long): Transaction? = null
        override suspend fun deleteFutureBySubscriptionId(subscriptionId: Long, afterDate: Long) = Unit
        override fun getTransactionsWithCoordinatesByMonth(startMs: Long, endMs: Long): Flow<List<TransactionWithMapData>> = flowOf(emptyList())
        override suspend fun insert(transaction: Transaction): Long {
            insertCalls++
            return 1L
        }
        override suspend fun update(transaction: Transaction) = Unit
        override suspend fun delete(transaction: Transaction) = Unit
    }
}
