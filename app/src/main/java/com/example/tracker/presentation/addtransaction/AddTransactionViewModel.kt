package com.example.tracker.presentation.addtransaction

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.db.dao.ProcessedNotificationDao
import com.example.tracker.data.enums.CategoryType
import com.example.tracker.data.enums.TransactionType
import com.example.tracker.data.model.Account
import com.example.tracker.data.model.Category
import com.example.tracker.data.model.ProcessedNotification
import com.example.tracker.data.model.Transaction
import com.example.tracker.data.preferences.ThemePreferences
import com.example.tracker.data.preferences.YapePreferences
import com.example.tracker.domain.exception.DuplicateTransactionException
import com.example.tracker.domain.usecase.account.GetAccountsUseCase
import com.example.tracker.domain.usecase.category.GetCategoriesUseCase
import com.example.tracker.domain.usecase.transaction.CreateTransactionUseCase
import com.example.tracker.domain.usecase.transaction.GetCategorySummaryUseCase
import com.example.tracker.domain.usecase.yape.ProcessYapeShareImageUseCase
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val getCategories: GetCategoriesUseCase,
    private val getAccounts: GetAccountsUseCase,
    private val createTransaction: CreateTransactionUseCase,
    private val themePreferences: ThemePreferences,
    private val processYapeShareImage: ProcessYapeShareImageUseCase,
    private val yapePreferences: YapePreferences,
    private val processedNotificationDao: ProcessedNotificationDao,
    private val getCategorySummary: GetCategorySummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState

    private var yapeDateMillis: Long? = null
    private var categorySummaryJob: Job? = null

    init {
        loadCategories()
        loadAccounts()
        loadLocationPreference()
    }

    private fun loadLocationPreference() {
        viewModelScope.launch {
            themePreferences.isLocationEnabled.collect { enabled ->
                _uiState.update { it.copy(isLocationEnabled = enabled) }
            }
        }
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
        _uiState.update { it.copy(selectedCategory = category, categorySummary = null) }
        loadCategorySummary(category.id)
    }

    private fun loadCategorySummary(categoryId: Long) {
        categorySummaryJob?.cancel()
        categorySummaryJob = viewModelScope.launch {
            val yearMonth = YearMonth.now()
            val zone = ZoneId.systemDefault()
            val start = yearMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val end = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
            getCategorySummary(categoryId, start, end).collect { summary ->
                _uiState.update { it.copy(categorySummary = summary) }
            }
        }
    }

    fun clearCategory() {
        categorySummaryJob?.cancel()
        categorySummaryJob = null
        _uiState.update {
            it.copy(
                selectedCategory = null,
                amountString = "",
                description = "",
                submitError = null,
                categorySummary = null,
                selectedDate = System.currentTimeMillis()
            )
        }
    }

    fun onDateSelected(dateMillis: Long) {
        _uiState.update { it.copy(selectedDate = dateMillis) }
    }

    fun onLocationToggle(enabled: Boolean, lat: Double? = null, lng: Double? = null) {
        _uiState.update { it.copy(isLocationEnabled = enabled, latitude = lat, longitude = lng) }
        viewModelScope.launch { themePreferences.setLocationEnabled(enabled) }
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

    fun processYapeImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingYapeImage = true, submitError = null) }

            val result = processYapeShareImage(uri)

            result.onSuccess { ocrResult ->
                val amountDisplay = centsToDisplayString(ocrResult.amountCents)
                val description = if (ocrResult.recipientName != null) {
                    "Yape a ${ocrResult.recipientName}"
                } else {
                    "Yape"
                }

                yapeDateMillis = ocrResult.dateMillis

                val categoryExpenseId = yapePreferences.categoryExpenseId.first()
                val accountId = yapePreferences.defaultAccountId.first()

                val state = _uiState.value
                val expenseCategory = state.categories.find { it.id == categoryExpenseId }
                val yapeAccount = state.accounts.find { it.id == accountId }

                _uiState.update {
                    it.copy(
                        isProcessingYapeImage = false,
                        amountString = amountDisplay,
                        description = description,
                        selectedCategory = expenseCategory ?: it.selectedCategory,
                        selectedAccount = yapeAccount ?: it.selectedAccount,
                        yapeOperationNumber = ocrResult.operationNumber,
                        prefilledSource = "yape"
                    )
                }
            }

            result.onFailure { error ->
                val errorMessage = when (error) {
                    is DuplicateTransactionException -> "Esta transacción ya fue registrada (Op. ${error.operationNumber})"
                    else -> error.message ?: "Error procesando imagen"
                }
                _uiState.update { it.copy(isProcessingYapeImage = false, submitError = errorMessage) }
            }
        }
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

        val transactionDate = if (state.prefilledSource == "yape" && yapeDateMillis != null) {
            yapeDateMillis!!
        } else {
            state.selectedDate
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
                        date = transactionDate,
                        latitude = if (state.isLocationEnabled) state.latitude else null,
                        longitude = if (state.isLocationEnabled) state.longitude else null
                    )
                )

                if (state.yapeOperationNumber != null) {
                    val dedupKey = "yape_share_${state.yapeOperationNumber}"
                    processedNotificationDao.insert(
                        ProcessedNotification(
                            dedupKey = dedupKey,
                            operationNumber = state.yapeOperationNumber,
                            amount = amountInMinorUnits,
                            type = transactionType.name
                        )
                    )
                }

                _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, submitError = "Failed to save transaction") }
            }
        }
    }

    fun reset() {
        categorySummaryJob?.cancel()
        categorySummaryJob = null
        yapeDateMillis = null
        _uiState.update {
            it.copy(
                selectedCategory = null,
                amountString = "",
                description = "",
                isSubmitting = false,
                submitError = null,
                submitSuccess = false,
                latitude = null,
                longitude = null,
                isProcessingYapeImage = false,
                yapeOperationNumber = null,
                prefilledSource = null,
                categorySummary = null,
                selectedDate = System.currentTimeMillis()
            )
        }
    }

    private fun amountStringToMinorUnits(s: String): Long {
        if (s.isBlank() || s == "." || s == "0") return 0L
        return ((s.toDoubleOrNull() ?: 0.0) * 100).toLong()
    }

    private fun centsToDisplayString(amountCents: Long): String {
        val value = amountCents / 100.0
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.2f", value)
        }
    }
}
