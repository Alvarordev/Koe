package com.example.tracker.presentation.accounts

import com.example.tracker.data.model.Account
import com.example.tracker.data.model.FormalLoan
import com.example.tracker.data.model.relations.CasualLoanWithPerson

data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
    val casualLoanSummaries: List<CasualLoanSummaryWithPerson> = emptyList(),
    val formalLoans: List<FormalLoan> = emptyList(),
    val isLoading: Boolean = true
)

data class CasualLoanSummaryWithPerson(
    val personId: Long,
    val personName: String,
    val personEmoji: String?,
    val totalLend: Long,
    val totalBorrow: Long
)
