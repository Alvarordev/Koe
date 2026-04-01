package com.hazard.koe.data.repository.impl

import com.hazard.koe.data.db.dao.CasualLoanDao
import com.hazard.koe.data.db.dao.CasualLoanTransactionDao
import com.hazard.koe.data.db.dao.TransactionDao
import com.hazard.koe.data.enums.CasualLoanTxnType
import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.data.model.CasualLoan
import com.hazard.koe.data.model.CasualLoanTransaction
import com.hazard.koe.data.model.Transaction
import com.hazard.koe.data.model.relations.CasualLoanWithPerson
import com.hazard.koe.data.model.relations.PersonLoanSummary
import com.hazard.koe.domain.repository.CasualLoanRepository
import kotlinx.coroutines.flow.Flow

class CasualLoanRepositoryImpl(
    private val loanDao: CasualLoanDao,
    private val loanTxnDao: CasualLoanTransactionDao,
    private val transactionDao: TransactionDao
) : CasualLoanRepository {

    override fun getAll(): Flow<List<CasualLoanWithPerson>> = loanDao.getAll()

    override fun getActive(): Flow<List<CasualLoanWithPerson>> = loanDao.getActive()

    override fun getSummaryByPerson(): Flow<List<PersonLoanSummary>> = loanDao.getSummaryByPerson()

    override fun getByPerson(personId: Long): Flow<List<CasualLoan>> = loanDao.getByPerson(personId)

    override fun getTransactions(loanId: Long): Flow<List<CasualLoanTransaction>> = loanTxnDao.getByLoan(loanId)

    override suspend fun createLoan(loan: CasualLoan): Long = loanDao.insert(loan)

    override suspend fun recordPayment(loanId: Long, amount: Long, accountId: Long, note: String?): Long {
        // Insert a ledger transaction for the account — caller should supply the correct categoryId.
        // Using categoryId = 0 as a sentinel; caller is expected to handle category assignment.
        val txnId = transactionDao.insert(
            Transaction(
                type = TransactionType.EXPENSE,
                amount = amount,
                accountId = accountId,
                categoryId = 0L,
                date = System.currentTimeMillis()
            )
        )
        val loanTxn = CasualLoanTransaction(
            casualLoanId = loanId,
            transactionId = txnId,
            amount = amount,
            type = CasualLoanTxnType.REPAYMENT,
            note = note,
            date = System.currentTimeMillis()
        )
        return loanTxnDao.insert(loanTxn)
    }

    override suspend fun markPaidOff(id: Long) = loanDao.markPaidOff(id)
}
