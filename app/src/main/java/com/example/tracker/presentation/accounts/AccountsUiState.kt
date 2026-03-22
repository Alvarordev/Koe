package com.example.tracker.presentation.accounts

import com.example.tracker.data.model.Account

data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = true
)
