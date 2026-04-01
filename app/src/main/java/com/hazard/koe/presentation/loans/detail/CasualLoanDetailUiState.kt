package com.hazard.koe.presentation.loans.detail

import com.hazard.koe.data.model.CasualLoan
import com.hazard.koe.data.model.CasualLoanTransaction
import com.hazard.koe.data.model.Person

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