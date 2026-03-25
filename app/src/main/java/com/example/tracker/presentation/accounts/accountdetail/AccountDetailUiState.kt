package com.example.tracker.presentation.accounts.accountdetail

import com.example.tracker.data.model.Account
import com.example.tracker.data.model.relations.TransactionWithDetails

data class AccountDetailUiState(
    val account: Account? = null,
    val transactions: List<TransactionWithDetails> = emptyList(),
    val isLoading: Boolean = true,
    val showDeleteDialog: Boolean = false,
    val isArchived: Boolean = false
)
