package com.hazard.koe.presentation.accounts.accountdetail

import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.relations.TransactionWithDetails

data class AccountDetailUiState(
    val account: Account? = null,
    val transactions: List<TransactionWithDetails> = emptyList(),
    val balanceHistory: List<BalanceHistoryPoint> = emptyList(),
    val isLoading: Boolean = true,
    val showDeleteDialog: Boolean = false,
    val isArchived: Boolean = false
)
