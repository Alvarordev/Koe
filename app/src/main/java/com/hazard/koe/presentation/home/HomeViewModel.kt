package com.hazard.koe.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.Category
import com.hazard.koe.data.model.Transaction
import com.hazard.koe.data.model.relations.TransactionWithDetails
import com.hazard.koe.domain.usecase.account.GetAccountsUseCase
import com.hazard.koe.domain.usecase.account.GetTotalAccountBalanceUseCase
import com.hazard.koe.domain.usecase.category.GetCategoriesUseCase
import com.hazard.koe.domain.usecase.home.ObserveHomeDateFilterPresetUseCase
import com.hazard.koe.domain.usecase.home.SaveHomeDateFilterPresetUseCase
import com.hazard.koe.domain.usecase.transaction.DeleteTransactionUseCase
import com.hazard.koe.domain.usecase.transaction.GetTransactionsByDateRangeUseCase
import com.hazard.koe.presentation.util.CurrencyFormatter
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class DayGroup(
    val date: LocalDate,
    val transactions: List<TransactionWithDetails>
)

private data class HomeBaseData(
    val transactions: List<TransactionWithDetails>,
    val categories: List<Category>,
    val accounts: List<Account>,
    val totalBalance: Long
)

data class HomeUiState(
    val dayGroups: List<DayGroup> = emptyList(),
    val availableCategories: List<Category> = emptyList(),
    val availableAccounts: List<Account> = emptyList(),
    val selectedCategoryIds: Set<Long> = emptySet(),
    val selectedAccountIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val dateFilterMode: DateFilterMode = DateFilterMode.Month,
    val showDateFilterDialog: Boolean = false,
    val showFilterSheet: Boolean = false,
    val expense: Double = 0.0,
    val income: Double = 0.0,
    val totalAccountBalance: Double = 0.0
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    getTransactionsByDateRange: GetTransactionsByDateRangeUseCase,
    getCategories: GetCategoriesUseCase,
    getAccounts: GetAccountsUseCase,
    getTotalAccountBalance: GetTotalAccountBalanceUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    observeHomeDateFilterPreset: ObserveHomeDateFilterPresetUseCase,
    private val saveHomeDateFilterPreset: SaveHomeDateFilterPresetUseCase
) : ViewModel() {

    companion object {
        const val ACCOUNT_FILTER_REQUEST_KEY = "home_account_filter_request"
    }

    private val _dateFilterMode = MutableStateFlow<DateFilterMode>(DateFilterMode.Month)
    private val _showDateFilterDialog = MutableStateFlow(false)
    private val _showFilterSheet = MutableStateFlow(false)
    private val _selectedCategoryIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _selectedAccountIds = MutableStateFlow<Set<Long>>(emptySet())

    init {
        viewModelScope.launch {
            observeHomeDateFilterPreset().collect { preset ->
                _dateFilterMode.value = DateFilterMode.fromPreset(preset)
            }
        }
    }

    val uiState: StateFlow<HomeUiState> = _dateFilterMode
        .flatMapLatest { mode ->
            val (start, end) = mode.dateRange()
            val baseData = combine(
                getTransactionsByDateRange(start, end),
                getCategories(),
                getAccounts(),
                getTotalAccountBalance()
            ) { transactions, categories, accounts, totalBalance ->
                HomeBaseData(
                    transactions = transactions,
                    categories = categories,
                    accounts = accounts,
                    totalBalance = totalBalance
                )
            }

            combine(
                baseData,
                _showDateFilterDialog,
                _showFilterSheet,
                _selectedCategoryIds,
                _selectedAccountIds
            ) { base, showDateDialog, showFilterSheet, selectedCategoryIds, selectedAccountIds ->
                val availableCategories = base.categories
                    .filter { !it.isArchived }
                    .sortedBy { it.sortOrder }
                val availableAccounts = base.accounts
                    .filter { !it.isArchived }
                    .sortedBy { it.sortOrder }

                val validCategoryIds = availableCategories.map { it.id }.toSet()
                val validAccountIds = availableAccounts.map { it.id }.toSet()
                val sanitizedSelectedCategoryIds = selectedCategoryIds.intersect(validCategoryIds)
                val sanitizedSelectedAccountIds = selectedAccountIds.intersect(validAccountIds)

                val filteredTransactions = base.transactions.filter { transactionWithDetails ->
                    val categoryMatch = sanitizedSelectedCategoryIds.isEmpty() ||
                            transactionWithDetails.category.id in sanitizedSelectedCategoryIds
                    val accountMatch = sanitizedSelectedAccountIds.isEmpty() ||
                            transactionWithDetails.account.id in sanitizedSelectedAccountIds
                    categoryMatch && accountMatch
                }

                val groups = filteredTransactions
                    .groupBy { CurrencyFormatter.toLocalDate(it.transaction.date) }
                    .entries
                    .sortedByDescending { it.key }
                    .map { (date, txns) -> DayGroup(date, txns) }

                val expenseTotal = filteredTransactions
                    .filter { it.transaction.type == TransactionType.EXPENSE }
                    .sumOf { it.transaction.amount }
                val incomeTotal = filteredTransactions
                    .filter { it.transaction.type == TransactionType.INCOME }
                    .sumOf { it.transaction.amount }

                HomeUiState(
                    dayGroups = groups,
                    availableCategories = availableCategories,
                    availableAccounts = availableAccounts,
                    selectedCategoryIds = sanitizedSelectedCategoryIds,
                    selectedAccountIds = sanitizedSelectedAccountIds,
                    isLoading = false,
                    dateFilterMode = mode,
                    showDateFilterDialog = showDateDialog,
                    showFilterSheet = showFilterSheet,
                    expense = expenseTotal / 100.0,
                    income = incomeTotal / 100.0,
                    totalAccountBalance = base.totalBalance / 100.0
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    fun onDateFilterSelected(mode: DateFilterMode) {
        _dateFilterMode.value = mode
        _showDateFilterDialog.value = false
        mode.toPersistablePresetOrNull()?.let { preset ->
            viewModelScope.launch {
                saveHomeDateFilterPreset(preset)
            }
        }
    }

    fun onToggleDateFilterDialog() {
        _showDateFilterDialog.value = !_showDateFilterDialog.value
    }

    fun onDismissDateFilterDialog() {
        _showDateFilterDialog.value = false
    }

    fun onToggleFilterSheet() {
        _showFilterSheet.value = !_showFilterSheet.value
    }

    fun onDismissFilterSheet() {
        _showFilterSheet.value = false
    }

    fun onToggleCategoryFilter(categoryId: Long) {
        _selectedCategoryIds.value = _selectedCategoryIds.value.toMutableSet().apply {
            if (contains(categoryId)) remove(categoryId) else add(categoryId)
        }
    }

    fun onToggleAccountFilter(accountId: Long) {
        _selectedAccountIds.value = _selectedAccountIds.value.toMutableSet().apply {
            if (contains(accountId)) remove(accountId) else add(accountId)
        }
    }

    fun onRemoveCategoryFilter(categoryId: Long) {
        _selectedCategoryIds.value = _selectedCategoryIds.value - categoryId
    }

    fun onRemoveAccountFilter(accountId: Long) {
        _selectedAccountIds.value = _selectedAccountIds.value - accountId
    }

    fun applySingleAccountFilter(accountId: Long) {
        _selectedAccountIds.value = setOf(accountId)
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            deleteTransactionUseCase(transaction)
        }
    }
}
