package com.hazard.koe.presentation.accounts.accountdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazard.koe.domain.usecase.account.ArchiveAccountUseCase
import com.hazard.koe.domain.usecase.account.GetBalanceHistoryUseCase
import com.hazard.koe.domain.usecase.account.GetAccountByIdUseCase
import com.hazard.koe.domain.usecase.transaction.GetTransactionsByAccountUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountDetailViewModel(
    private val accountId: Long,
    getAccountById: GetAccountByIdUseCase,
    getTransactionsByAccount: GetTransactionsByAccountUseCase,
    getBalanceHistory: GetBalanceHistoryUseCase,
    private val archiveAccount: ArchiveAccountUseCase
) : ViewModel() {

    private val _showDeleteDialog = MutableStateFlow(false)
    private val _isArchived = MutableStateFlow(false)

    val uiState: StateFlow<AccountDetailUiState> = combine(
        getAccountById(accountId),
        getTransactionsByAccount(accountId),
        getBalanceHistory(accountId),
        _showDeleteDialog,
        _isArchived
    ) { account, transactions, balanceHistory, showDelete, archived ->
        AccountDetailUiState(
            account = account,
            transactions = transactions,
            balanceHistory = balanceHistory,
            isLoading = false,
            showDeleteDialog = showDelete,
            isArchived = archived
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountDetailUiState()
    )

    fun showDeleteDialog() {
        _showDeleteDialog.value = true
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun archiveAccount() {
        viewModelScope.launch {
            archiveAccount(accountId)
            _isArchived.value = true
        }
    }
}
