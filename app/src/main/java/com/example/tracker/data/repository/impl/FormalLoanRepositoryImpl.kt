package com.example.tracker.data.repository.impl

import com.example.tracker.data.db.dao.FormalLoanDao
import com.example.tracker.data.db.dao.FormalLoanPaymentDao
import com.example.tracker.data.db.dao.TransactionDao
import com.example.tracker.data.enums.PaymentStatus
import com.example.tracker.data.model.FormalLoan
import com.example.tracker.data.model.FormalLoanPayment
import com.example.tracker.domain.repository.FormalLoanRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FormalLoanRepositoryImpl(
    private val loanDao: FormalLoanDao,
    private val paymentDao: FormalLoanPaymentDao,
    private val transactionDao: TransactionDao
) : FormalLoanRepository {

    override fun getAll(): Flow<List<FormalLoan>> = loanDao.getAll()

    override fun getActive(): Flow<List<FormalLoan>> = loanDao.getActive()

    override fun getById(id: Long): Flow<FormalLoan?> = loanDao.getById(id)

    override fun getPayments(loanId: Long): Flow<List<FormalLoanPayment>> = paymentDao.getByLoan(loanId)

    override fun getOverduePayments(): Flow<List<FormalLoanPayment>> =
        paymentDao.getOverdue(System.currentTimeMillis())

    override suspend fun create(loan: FormalLoan): Long {
        val id = loanDao.insert(loan)
        val schedule = generatePaymentSchedule(loan.copy(id = id))
        paymentDao.insertAll(schedule)
        return id
    }

    override suspend fun update(loan: FormalLoan) = loanDao.update(loan)

    override suspend fun recordPayment(paymentId: Long, accountId: Long): Long {
        val now = System.currentTimeMillis()
        paymentDao.updateStatus(paymentId, PaymentStatus.PAID, now, null)
        return paymentId
    }

    private fun generatePaymentSchedule(loan: FormalLoan): List<FormalLoanPayment> {
        val payments = mutableListOf<FormalLoanPayment>()
        var balance = loan.principalAmount.toDouble()
        val cal = Calendar.getInstance().apply { timeInMillis = loan.startDate }
        for (i in 1..loan.termMonths) {
            cal.add(Calendar.MONTH, 1)
            cal.set(
                Calendar.DAY_OF_MONTH,
                minOf(loan.paymentDayOfMonth, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            )
            val interest = (balance * loan.monthlyRate).toLong()
            val principal = loan.monthlyPayment - interest
            payments.add(
                FormalLoanPayment(
                    formalLoanId = loan.id,
                    paymentNumber = i,
                    dueDate = cal.timeInMillis,
                    principalPortion = principal,
                    interestPortion = interest,
                    totalAmount = loan.monthlyPayment,
                    status = PaymentStatus.PENDING
                )
            )
            balance -= principal
            if (balance <= 0) break
        }
        return payments
    }
}
