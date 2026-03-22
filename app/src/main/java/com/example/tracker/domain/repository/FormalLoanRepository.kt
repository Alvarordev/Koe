package com.example.tracker.domain.repository

import com.example.tracker.data.model.FormalLoan
import com.example.tracker.data.model.FormalLoanPayment
import kotlinx.coroutines.flow.Flow

interface FormalLoanRepository {
    fun getAll(): Flow<List<FormalLoan>>
    fun getActive(): Flow<List<FormalLoan>>
    fun getById(id: Long): Flow<FormalLoan?>
    fun getPayments(loanId: Long): Flow<List<FormalLoanPayment>>
    fun getOverduePayments(): Flow<List<FormalLoanPayment>>
    suspend fun create(loan: FormalLoan): Long
    suspend fun update(loan: FormalLoan)
    suspend fun recordPayment(paymentId: Long, accountId: Long): Long
}
