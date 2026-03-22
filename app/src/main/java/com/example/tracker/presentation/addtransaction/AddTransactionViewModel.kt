package com.example.tracker.presentation.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.enums.CategoryType
import com.example.tracker.data.enums.TransactionType
import com.example.tracker.data.model.Account
import com.example.tracker.data.model.Category
import com.example.tracker.data.model.Transaction
import com.example.tracker.domain.usecase.account.GetAccountsUseCase
import com.example.tracker.domain.usecase.category.GetCategoriesUseCase
import com.example.tracker.domain.usecase.transaction.CreateTransactionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val getCategories: GetCategoriesUseCase,
    private val getAccounts: GetAccountsUseCase,
    private val createTransaction: CreateTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState

    init {
        loadCategories()
        loadAccounts()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            getAccounts().collect { accounts ->
                val defaultAccount = accounts
                    .filter { !it.isArchived }
                    .minByOrNull { it.sortOrder }
                _uiState.update { it.copy(accounts = accounts, selectedAccount = it.selectedAccount ?: defaultAccount) }
            }
        }
    }

    fun selectCategory(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun clearCategory() {
        _uiState.update { it.copy(selectedCategory = null, amountString = "", description = "", submitError = null) }
    }

    fun selectAccount(account: Account) {
        _uiState.update { it.copy(selectedAccount = account) }
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

    fun submit() {
        val state = _uiState.value
        if (state.isSubmitting) return

        val account = state.selectedAccount
        if (account == null) {
            _uiState.update { it.copy(submitError = "Please add an account first") }
            return
        }

        val category = state.selectedCategory
        if (category == null) {
            _uiState.update { it.copy(submitError = "Please select a category") }
            return
        }

        val amountInMinorUnits = amountStringToMinorUnits(state.amountString)
        if (amountInMinorUnits == 0L) {
            _uiState.update { it.copy(submitError = "Please enter an amount") }
            return
        }

        val transactionType = when (category.type) {
            CategoryType.EXPENSE -> TransactionType.EXPENSE
            CategoryType.INCOME -> TransactionType.INCOME
        }

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }

        viewModelScope.launch {
            try {
                createTransaction(
                    Transaction(
                        type = transactionType,
                        amount = amountInMinorUnits,
                        description = state.description.ifBlank { null },
                        accountId = account.id,
                        categoryId = category.id,
                        date = System.currentTimeMillis()
                    )
                )
                _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, submitError = "Failed to save transaction") }
            }
        }
    }

    fun reset() {
        _uiState.update {
            it.copy(
                selectedCategory = null,
                amountString = "",
                description = "",
                isSubmitting = false,
                submitError = null,
                submitSuccess = false
            )
        }
    }

    private fun amountStringToMinorUnits(s: String): Long {
        if (s.isBlank() || s == "." || s == "0") return 0L
        return ((s.toDoubleOrNull() ?: 0.0) * 100).toLong()
    }
}
