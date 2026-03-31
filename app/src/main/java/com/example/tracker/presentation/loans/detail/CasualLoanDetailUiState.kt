package com.example.tracker.presentation.loans.detail

import com.example.tracker.data.model.CasualLoan
import com.example.tracker.data.model.CasualLoanTransaction
import com.example.tracker.data.model.Person
import com.example.tracker.data.enums.LoanDirection

data class CasualLoanDetailUiState(
    val person: Person? = null,
    val loans: List<CasualLoanWithTransactions> = emptyList(),
    val totalLend: Long = 0L,
    val totalBorrow: Long = 0L,
    val isLoading: Boolean = true
)

data class CasualLoanWithTransactions(
    val loan: CasualLoan,
    val transactions: List<CasualLoanTransaction> = emptyList()
)