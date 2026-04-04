package com.hazard.koe.data.repository.impl

import androidx.room.withTransaction
import com.hazard.koe.data.db.TrackerDatabase
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
import com.hazard.koe.domain.exception.IncomeNotAllowedForCreditAccountException
import com.hazard.koe.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val database: TrackerDatabase? = null
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
        return inTransaction {
            validateApplyTransactionEffect(transaction)
            val id = transactionDao.insert(transaction)
            applyTransactionEffect(transaction)
            id
        }
    }

    override suspend fun update(transaction: Transaction) {
        inTransaction {
            val existing = transactionDao.getById(transaction.id).first()?.transaction
            if (existing != null) {
                revertTransactionEffect(existing)
                validateApplyTransactionEffect(transaction)
                transactionDao.update(transaction)
                applyTransactionEffect(transaction)
            }
        }
    }

    override suspend fun delete(transaction: Transaction) {
        inTransaction {
            val existing = transactionDao.getById(transaction.id).first()?.transaction
            if (existing != null) {
                revertTransactionEffect(existing)
                transactionDao.delete(existing)
            }
        }
    }

    override suspend fun getLastBySubscriptionId(subscriptionId: Long): Transaction? =
        transactionDao.getLastBySubscriptionId(subscriptionId)

    override suspend fun deleteFutureBySubscriptionId(subscriptionId: Long, afterDate: Long) =
        transactionDao.deleteFutureBySubscriptionId(subscriptionId, afterDate)

    override fun getTransactionsWithCoordinatesByMonth(startMs: Long, endMs: Long): Flow<List<TransactionWithMapData>> =
        transactionDao.getTransactionsWithCoordinatesByMonth(startMs, endMs)

    private suspend fun validateApplyTransactionEffect(transaction: Transaction) {
        val account = accountDao.getById(transaction.accountId).first() ?: return

        if (transaction.type == TransactionType.INCOME && account.type == AccountType.CREDIT) {
            throw IncomeNotAllowedForCreditAccountException()
        }

        if (transaction.type != TransactionType.EXPENSE) return

        if (account.type == AccountType.CREDIT) {
            val newCreditUsed = (account.creditUsed ?: 0L) + transaction.amount
            val creditLimit = account.creditLimit ?: 0L
            if (newCreditUsed > creditLimit) {
                throw CreditLimitExceededException()
            }
        }
    }

    private suspend fun applyTransactionEffect(transaction: Transaction) {
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                val account = accountDao.getById(transaction.accountId).first() ?: return
                if (account.type == AccountType.CREDIT) {
                    val newCreditUsed = (account.creditUsed ?: 0L) + transaction.amount
                    accountDao.updateCreditUsed(transaction.accountId, newCreditUsed)
                } else {
                    accountDao.updateBalance(transaction.accountId, account.currentBalance - transaction.amount)
                }
            }

            TransactionType.INCOME -> {
                val account = accountDao.getById(transaction.accountId).first() ?: return
                accountDao.updateBalance(transaction.accountId, account.currentBalance + transaction.amount)
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
    }

    private suspend fun revertTransactionEffect(transaction: Transaction) {
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                val account = accountDao.getById(transaction.accountId).first() ?: return
                if (account.type == AccountType.CREDIT) {
                    val newCreditUsed = (account.creditUsed ?: 0L) - transaction.amount
                    accountDao.updateCreditUsed(transaction.accountId, maxOf(0L, newCreditUsed))
                } else {
                    accountDao.updateBalance(transaction.accountId, account.currentBalance + transaction.amount)
                }
            }

            TransactionType.INCOME -> {
                val account = accountDao.getById(transaction.accountId).first() ?: return
                accountDao.updateBalance(transaction.accountId, account.currentBalance - transaction.amount)
            }

            TransactionType.TRANSFER -> {
                val srcAccount = accountDao.getById(transaction.accountId).first()
                if (srcAccount != null) {
                    accountDao.updateBalance(transaction.accountId, srcAccount.currentBalance + transaction.amount)
                }
                transaction.transferToAccountId?.let { destId ->
                    val destAccount = accountDao.getById(destId).first()
                    if (destAccount != null) {
                        val received = transaction.convertedAmount ?: transaction.amount
                        accountDao.updateBalance(destId, destAccount.currentBalance - received)
                    }
                }
            }

            TransactionType.CREDIT_CARD_PAYMENT -> {
                val srcAccount = accountDao.getById(transaction.accountId).first()
                if (srcAccount != null) {
                    accountDao.updateBalance(transaction.accountId, srcAccount.currentBalance + transaction.amount)
                }
                transaction.transferToAccountId?.let { creditId ->
                    val creditAccount = accountDao.getById(creditId).first()
                    if (creditAccount != null) {
                        val newCreditUsed = (creditAccount.creditUsed ?: 0L) + transaction.amount
                        accountDao.updateCreditUsed(creditId, newCreditUsed)
                    }
                }
            }
        }
    }

    private suspend fun <T> inTransaction(block: suspend () -> T): T {
        return if (database != null) {
            database.withTransaction { block() }
        } else {
            block()
        }
    }
}
