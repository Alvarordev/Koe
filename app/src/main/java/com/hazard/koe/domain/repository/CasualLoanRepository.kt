package com.hazard.koe.domain.repository

import com.hazard.koe.data.model.CasualLoan
import com.hazard.koe.data.model.CasualLoanTransaction
import com.hazard.koe.data.model.relations.CasualLoanWithPerson
import com.hazard.koe.data.model.relations.PersonLoanSummary
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
