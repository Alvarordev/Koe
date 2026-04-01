package com.hazard.koe.presentation.accounts

import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.FormalLoan

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
