package com.example.tracker.presentation.accounts.accountdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.domain.usecase.account.GetAccountByIdUseCase
import com.example.tracker.domain.usecase.transaction.GetTransactionsByAccountUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AccountDetailViewModel(
    accountId: Long,
    getAccountById: GetAccountByIdUseCase,
    getTransactionsByAccount: GetTransactionsByAccountUseCase
) : ViewModel() {

    val uiState: StateFlow<AccountDetailUiState> = combine(
        getAccountById(accountId),
        getTransactionsByAccount(accountId)
    ) { account, transactions ->
        AccountDetailUiState(
            account = account,
            transactions = transactions,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountDetailUiState()
    )
}
