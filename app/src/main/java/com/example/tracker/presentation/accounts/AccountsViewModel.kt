package com.example.tracker.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.domain.usecase.account.GetAccountsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AccountsViewModel(
    getAccounts: GetAccountsUseCase
) : ViewModel() {

    val uiState: StateFlow<AccountsUiState> = getAccounts()
        .map { accounts ->
            AccountsUiState(
                accounts = accounts.filter { !it.isArchived },
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AccountsUiState()
        )
}
