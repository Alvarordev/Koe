package com.hazard.koe.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hazard.koe.data.enums.PaymentStatus
import com.hazard.koe.data.model.FormalLoanPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface FormalLoanPaymentDao {

    @Query("SELECT * FROM formal_loan_payments WHERE formalLoanId = :loanId ORDER BY paymentNumber ASC")
    fun getByLoan(loanId: Long): Flow<List<FormalLoanPayment>>

    @Query("SELECT * FROM formal_loan_payments WHERE formalLoanId = :loanId AND status = 'PENDING' ORDER BY paymentNumber ASC")
    fun getPending(loanId: Long): Flow<List<FormalLoanPayment>>

    @Query("SELECT * FROM formal_loan_payments WHERE status = 'PENDING' AND dueDate < :beforeDate")
    fun getOverdue(beforeDate: Long): Flow<List<FormalLoanPayment>>

    @Query("SELECT * FROM formal_loan_payments ORDER BY formalLoanId ASC, paymentNumber ASC")
    suspend fun getAllRaw(): List<FormalLoanPayment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: FormalLoanPayment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<FormalLoanPayment>)

    @Query("UPDATE formal_loan_payments SET status = :status, paidDate = :paidDate, transactionId = :transactionId WHERE id = :id")
    suspend fun updateStatus(id: Long, status: PaymentStatus, paidDate: Long?, transactionId: Long?)
}
