package com.hazard.koe.presentation.addtransaction

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazard.koe.data.db.dao.ProcessedNotificationDao
import com.hazard.koe.data.enums.CategoryType
import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.Category
import com.hazard.koe.data.model.ProcessedNotification
import com.hazard.koe.data.model.Transaction
import com.hazard.koe.data.preferences.ThemePreferences
import com.hazard.koe.data.preferences.YapePreferences
import com.hazard.koe.domain.exception.DuplicateTransactionException
import com.hazard.koe.domain.exception.CreditLimitExceededException
import com.hazard.koe.domain.repository.TransactionRepository
import com.hazard.koe.domain.usecase.account.GetAccountsUseCase
import com.hazard.koe.domain.usecase.category.GetCategoriesUseCase
import com.hazard.koe.data.model.relations.CategorySummary
import com.hazard.koe.domain.usecase.transaction.CreateTransactionUseCase
import com.hazard.koe.domain.usecase.transaction.GetAllCategorySummariesUseCase
import com.hazard.koe.domain.usecase.transaction.GetCategorySummaryUseCase
import com.hazard.koe.domain.usecase.transaction.UpdateTransactionUseCase
import com.hazard.koe.domain.usecase.yape.ProcessYapeShareImageUseCase
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
    private val updateTransaction: UpdateTransactionUseCase,
    private val transactionRepository: TransactionRepository,
    private val themePreferences: ThemePreferences,
    private val processYapeShareImage: ProcessYapeShareImageUseCase,
    private val yapePreferences: YapePreferences,
    private val processedNotificationDao: ProcessedNotificationDao,
    private val getCategorySummary: GetCategorySummaryUseCase,
    private val getAllCategorySummaries: GetAllCategorySummariesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState

    private var yapeDateMillis: Long? = null
    private var categorySummaryJob: Job? = null
    private var editingTransactionId: Long? = null
    private var editingTransactionCreatedAt: Long? = null
    private var loadTransactionJob: Job? = null

    init {
        loadCategories()
        loadAccounts()
        loadLocationPreference()
        loadAllCategorySummaries()
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

    private fun loadAllCategorySummaries() {
        viewModelScope.launch {
            val yearMonth = YearMonth.now()
            val zone = ZoneId.systemDefault()
            val start = yearMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val end = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
            getAllCategorySummaries(start, end).collect { summaries ->
                _uiState.update { state ->
                    state.copy(categorySummaries = summaries.associate { it.categoryId to CategorySummary(it.count, it.total) })
                }
            }
        }
    }

    fun selectCategory(category: Category) {
        _uiState.update { it.copy(selectedCategory = category, categorySummary = null, categoryError = false) }
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
        editingTransactionId = null
        editingTransactionCreatedAt = null
        loadTransactionJob?.cancel()
        loadTransactionJob = null
        yapeDateMillis = null
        _uiState.update {
            it.copy(
                selectedCategory = null,
                amountString = "",
                description = "",
                submitError = null,
                categoryError = false,
                categorySummary = null,
                prefilledSource = null,
                yapeOperationNumber = null,
                selectedDate = System.currentTimeMillis()
            )
        }
    }

    fun loadTransactionForEdit(transaction: Transaction, account: Account, category: Category) {
        editingTransactionId = transaction.id
        editingTransactionCreatedAt = transaction.createdAt
        yapeDateMillis = null
        val amountDisplay = centsToDisplayString(transaction.amount)
        _uiState.update {
            it.copy(
                amountString = amountDisplay,
                description = transaction.description ?: "",
                selectedAccount = account,
                selectedCategory = category,
                selectedDate = transaction.date,
                isLocationEnabled = transaction.latitude != null && transaction.longitude != null,
                latitude = transaction.latitude,
                longitude = transaction.longitude,
                submitError = null,
                submitSuccess = false,
                categorySummary = null,
                prefilledSource = null,
                yapeOperationNumber = null
            )
        }
        loadCategorySummary(category.id)
    }

    fun getEditingTransactionId(): Long? = editingTransactionId

    fun loadTransactionById(transactionId: Long) {
        loadTransactionJob?.cancel()
        loadTransactionJob = viewModelScope.launch {
            val transactionWithDetails = transactionRepository.getById(transactionId).first()
            if (transactionWithDetails != null) {
                loadTransactionForEdit(
                    transaction = transactionWithDetails.transaction,
                    account = transactionWithDetails.account,
                    category = transactionWithDetails.category
                )
            }
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
            _uiState.update { it.copy(categoryError = true) }
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
                val isEditing = editingTransactionId != null
                val transaction = Transaction(
                    id = editingTransactionId ?: 0L,
                    type = transactionType,
                    amount = amountInMinorUnits,
                    description = state.description.ifBlank { null },
                    accountId = account.id,
                    categoryId = category.id,
                    date = transactionDate,
                    latitude = if (state.isLocationEnabled) state.latitude else null,
                    longitude = if (state.isLocationEnabled) state.longitude else null,
                    createdAt = if (isEditing) (editingTransactionCreatedAt ?: System.currentTimeMillis()) else System.currentTimeMillis()
                )

                if (isEditing) {
                    updateTransaction(transaction)
                } else {
                    createTransaction(transaction)

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
                }

                _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
            } catch (e: Exception) {
                val error = when (e) {
                    is CreditLimitExceededException -> "No disponible: excede el límite de crédito"
                    else -> "Failed to save transaction"
                }
                _uiState.update { it.copy(isSubmitting = false, submitError = error) }
            }
        }
    }

    fun reset() {
        categorySummaryJob?.cancel()
        categorySummaryJob = null
        yapeDateMillis = null
        editingTransactionId = null
        editingTransactionCreatedAt = null
        loadTransactionJob?.cancel()
        loadTransactionJob = null
        _uiState.update {
            it.copy(
                selectedCategory = null,
                amountString = "",
                description = "",
                isSubmitting = false,
                submitError = null,
                categoryError = false,
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
        val normalized = s.trim()
        if (normalized.isBlank() || normalized == "." || normalized == "0") return 0L

        val parts = normalized.split('.', limit = 2)
        val whole = parts.getOrNull(0).orEmpty().ifBlank { "0" }
        val wholePart = whole.toLongOrNull() ?: return 0L

        val fractionalRaw = parts.getOrNull(1).orEmpty()
        val fractionalPart = when {
            fractionalRaw.isEmpty() -> 0L
            fractionalRaw.length == 1 -> (fractionalRaw + "0").toLongOrNull() ?: 0L
            else -> fractionalRaw.take(2).toLongOrNull() ?: 0L
        }

        return (wholePart * 100L) + fractionalPart
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
