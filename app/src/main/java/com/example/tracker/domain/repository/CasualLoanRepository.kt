package com.example.tracker.domain.repository

import com.example.tracker.data.model.CasualLoan
import com.example.tracker.data.model.CasualLoanTransaction
import com.example.tracker.data.model.relations.CasualLoanWithPerson
import com.example.tracker.data.model.relations.PersonLoanSummary
import kotlinx.coroutines.flow.Flow

interface CasualLoanRepository {
    fun getAll(): Flow<List<CasualLoanWithPerson>>
    fun getActive(): Flow<List<CasualLoanWithPerson>>
    fun getSummaryByPerson(): Flow<List<PersonLoanSummary>>
    fun getByPerson(personId: Long): Flow<List<CasualLoan>>
    fun getTransactions(loanId: Long): Flow<List<CasualLoanTransaction>>
    suspend fun createLoan(loan: CasualLoan): Long
    suspend fun recordPayment(loanId: Long, amount: Long, accountId: Long, note: String?): Long
    suspend fun markPaidOff(id: Long)
}
