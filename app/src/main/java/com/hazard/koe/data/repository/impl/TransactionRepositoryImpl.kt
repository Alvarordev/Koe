package com.hazard.koe.data.repository.impl

import com.hazard.koe.data.db.dao.AccountDao
import com.hazard.koe.data.db.dao.TransactionDao
import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.data.model.Transaction
import com.hazard.koe.data.model.relations.CategoryIdSummary
import com.hazard.koe.data.model.relations.CategorySummary
import com.hazard.koe.data.model.relations.CategoryTotal
import com.hazard.koe.data.model.relations.TransactionWithDetails
import com.hazard.koe.data.model.relations.TransactionWithMapData
import com.hazard.koe.domain.exception.CreditLimitExceededException
import com.hazard.koe.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao
) : TransactionRepository {

    override fun getAll(): Flow<List<TransactionWithDetails>> = transactionDao.getAll()

    override fun getById(id: Long): Flow<TransactionWithDetails?> = transactionDao.getById(id)

    override fun getByAccount(accountId: Long): Flow<List<TransactionWithDetails>> =
        transactionDao.getByAccount(accountId)

    override fun getByCategory(categoryId: Long): Flow<List<TransactionWithDetails>> =
        transactionDao.getByCategory(categoryId)

    override fun getByDateRange(start: Long, end: Long): Flow<List<TransactionWithDetails>> =
        transactionDao.getByDateRange(start, end)

    override fun getCategorySummaryInPeriod(categoryId: Long, start: Long, end: Long): Flow<CategorySummary> =
        transactionDao.getCategorySummaryInPeriod(categoryId, start, end)

    override fun getAllCategorySummariesInPeriod(start: Long, end: Long): Flow<List<CategoryIdSummary>> =
        transactionDao.getAllCategorySummariesInPeriod(start, end)

    override fun getExpensesByCategoryInPeriod(start: Long, end: Long): Flow<List<CategoryTotal>> =
        transactionDao.getExpensesByCategoryInPeriod(start, end)

    override fun getTotalByTypeInPeriod(type: TransactionType, start: Long, end: Long): Flow<Long> =
        transactionDao.getTotalByTypeInPeriod(type, start, end)

    override suspend fun create(transaction: Transaction): Long {
        if (transaction.type == TransactionType.EXPENSE) {
            val account = accountDao.getById(transaction.accountId).first()
            if (account != null && account.type == AccountType.CREDIT) {
                val newCreditUsed = (account.creditUsed ?: 0L) + transaction.amount
                val creditLimit = account.creditLimit ?: 0L
                if (newCreditUsed > creditLimit) {
                    throw CreditLimitExceededException()
                }
            }
        }

        val id = transactionDao.insert(transaction)
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                val account = accountDao.getById(transaction.accountId).first()
                if (account != null) {
                    if (account.type == AccountType.CREDIT) {
                        val newCreditUsed = (account.creditUsed ?: 0L) + transaction.amount
                        accountDao.updateCreditUsed(transaction.accountId, newCreditUsed)
                    } else {
                        accountDao.updateBalance(transaction.accountId, account.currentBalance - transaction.amount)
                    }
                }
            }
            TransactionType.INCOME -> {
                val account = accountDao.getById(transaction.accountId).first()
                if (account != null) {
                    accountDao.updateBalance(transaction.accountId, account.currentBalance + transaction.amount)
                }
            }
            TransactionType.TRANSFER -> {
                val srcAccount = accountDao.getById(transaction.accountId).first()
                if (srcAccount != null) {
                    accountDao.updateBalance(transaction.accountId, srcAccount.currentBalance - transaction.amount)
                }
                transaction.transferToAccountId?.let { destId ->
                    val destAccount = accountDao.getById(destId).first()
                    if (destAccount != null) {
                        val received = transaction.convertedAmount ?: transaction.amount
                        accountDao.updateBalance(destId, destAccount.currentBalance + received)
                    }
                }
            }
            TransactionType.CREDIT_CARD_PAYMENT -> {
                val srcAccount = accountDao.getById(transaction.accountId).first()
                if (srcAccount != null) {
                    accountDao.updateBalance(transaction.accountId, srcAccount.currentBalance - transaction.amount)
                }
                transaction.transferToAccountId?.let { creditId ->
                    val creditAccount = accountDao.getById(creditId).first()
                    if (creditAccount != null) {
                        val newCreditUsed = (creditAccount.creditUsed ?: 0L) - transaction.amount
                        accountDao.updateCreditUsed(creditId, maxOf(0L, newCreditUsed))
                    }
                }
            }
        }
        return id
    }

    override suspend fun update(transaction: Transaction) = transactionDao.update(transaction)

    override suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)

    override suspend fun getLastBySubscriptionId(subscriptionId: Long): Transaction? =
        transactionDao.getLastBySubscriptionId(subscriptionId)

    override suspend fun deleteFutureBySubscriptionId(subscriptionId: Long, afterDate: Long) =
        transactionDao.deleteFutureBySubscriptionId(subscriptionId, afterDate)

    override fun getTransactionsWithCoordinatesByMonth(startMs: Long, endMs: Long): Flow<List<TransactionWithMapData>> =
        transactionDao.getTransactionsWithCoordinatesByMonth(startMs, endMs)
}
