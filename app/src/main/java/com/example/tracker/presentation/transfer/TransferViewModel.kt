package com.example.tracker.presentation.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.enums.TransactionType
import com.example.tracker.data.model.Account
import com.example.tracker.data.model.Transaction
import com.example.tracker.data.preferences.ExchangeRatePreferences
import com.example.tracker.domain.usecase.account.GetAccountsUseCase
import com.example.tracker.domain.usecase.category.GetTransferCategoryUseCase
import com.example.tracker.domain.usecase.transaction.CreateTransactionUseCase
import com.example.tracker.presentation.addtransaction.KeyboardKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransferViewModel(
    private val getAccounts: GetAccountsUseCase,
    private val createTransaction: CreateTransactionUseCase,
    private val getTransferCategory: GetTransferCategoryUseCase,
    private val exchangeRatePreferences: ExchangeRatePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState

    private var transferCategoryId: Long? = null

    init {
        loadAccounts()
        loadTransferCategory()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            getAccounts().collect { accounts ->
                _uiState.update { it.copy(accounts = accounts.filter { a -> !a.isArchived }) }
            }
        }
    }

    private fun loadTransferCategory() {
        viewModelScope.launch {
            transferCategoryId = getTransferCategory()?.id
        }
    }

    private fun updateExchangeRate() {
        val state = _uiState.value
        val source = state.sourceAccount ?: return
        val dest = state.destinationAccount ?: return
        val isCross = source.currencyCode != dest.currencyCode
        if (!isCross) {
            _uiState.update { it.copy(exchangeRate = 1.0, isCrossCurrency = false) }
            return
        }
        viewModelScope.launch {
            val rate = exchangeRatePreferences.getRate(source.currencyCode, dest.currencyCode)
            _uiState.update { it.copy(exchangeRate = rate, isCrossCurrency = true) }
        }
    }

    fun selectSourceAccount(account: Account) {
        _uiState.update {
            it.copy(
                sourceAccount = account,
                destinationAccount = if (it.destinationAccount?.id == account.id) null else it.destinationAccount
            )
        }
        updateExchangeRate()
    }

    fun selectDestinationAccount(account: Account) {
        val state = _uiState.value
        if (account.id == state.sourceAccount?.id) return
        _uiState.update { it.copy(destinationAccount = account) }
        updateExchangeRate()
    }

    fun onKeyPress(key: KeyboardKey) {
        val current = _uiState.value.amountString
        val updated = when (key) {
            is KeyboardKey.Digit -> {
                val dotIndex = current.indexOf('.')
                if (dotIndex >= 0 && current.length - dotIndex > 2) {
                    current
                } else if (current.isEmpty() || current == "0") {
                    key.char.toString()
                } else if (current.length >= 10) {
                    current
                } else {
                    current + key.char
                }
            }
            is KeyboardKey.Dot -> {
                if (current.contains('.')) current
                else if (current.isEmpty()) "0."
                else "$current."
            }
            is KeyboardKey.Delete -> {
                if (current.isEmpty() || current == "0") current
                else current.dropLast(1)
            }
            else -> current
        }
        _uiState.update { it.copy(amountString = updated, submitError = null) }
    }

    fun onDescriptionChange(text: String) {
        _uiState.update { it.copy(description = text) }
    }

    fun goBack() {
        _uiState.update {
            it.copy(
                sourceAccount = null,
                destinationAccount = null,
                amountString = "",
                description = "",
                submitError = null,
                exchangeRate = 1.0,
                isCrossCurrency = false
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val source = state.sourceAccount
        if (source == null) {
            _uiState.update { it.copy(submitError = "Select source account") }
            return
        }

        val destination = state.destinationAccount
        if (destination == null) {
            _uiState.update { it.copy(submitError = "Select destination account") }
            return
        }

        val categoryId = transferCategoryId
        if (categoryId == null) {
            _uiState.update { it.copy(submitError = "Transfer category not found") }
            return
        }

        val amountInMinorUnits = amountStringToMinorUnits(state.amountString)
        if (amountInMinorUnits == 0L) {
            _uiState.update { it.copy(submitError = "Enter an amount") }
            return
        }

        val isCross = source.currencyCode != destination.currencyCode
        val exchangeRate = if (isCross) state.exchangeRate else null
        val convertedAmount = if (isCross) {
            (amountInMinorUnits * state.exchangeRate).toLong()
        } else {
            null
        }

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }

        viewModelScope.launch {
            try {
                createTransaction(
                    Transaction(
                        type = TransactionType.TRANSFER,
                        amount = amountInMinorUnits,
                        description = state.description.ifBlank { null },
                        accountId = source.id,
                        transferToAccountId = destination.id,
                        categoryId = categoryId,
                        date = System.currentTimeMillis(),
                        exchangeRate = exchangeRate,
                        convertedAmount = convertedAmount
                    )
                )
                _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, submitError = "Failed to save transfer") }
            }
        }
    }

    fun reset() {
        _uiState.update {
            TransferUiState(accounts = it.accounts)
        }
    }

    private fun amountStringToMinorUnits(s: String): Long {
        if (s.isBlank() || s == "." || s == "0") return 0L
        return ((s.toDoubleOrNull() ?: 0.0) * 100).toLong()
    }
}
