package com.example.tracker.data.repository.impl

import com.example.tracker.data.db.dao.AccountDao
import com.example.tracker.data.db.dao.TransactionDao
import com.example.tracker.data.enums.TransactionType
import com.example.tracker.data.model.Transaction
import com.example.tracker.data.model.relations.CategoryTotal
import com.example.tracker.data.model.relations.TransactionWithDetails
import com.example.tracker.domain.repository.TransactionRepository
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

    override fun getExpensesByCategoryInPeriod(start: Long, end: Long): Flow<List<CategoryTotal>> =
        transactionDao.getExpensesByCategoryInPeriod(start, end)

    override fun getTotalByTypeInPeriod(type: TransactionType, start: Long, end: Long): Flow<Long> =
        transactionDao.getTotalByTypeInPeriod(type, start, end)

    override suspend fun create(transaction: Transaction): Long {
        val id = transactionDao.insert(transaction)
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                val account = accountDao.getById(transaction.accountId).first()
                if (account != null) {
                    accountDao.updateBalance(transaction.accountId, account.currentBalance - transaction.amount)
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
}
