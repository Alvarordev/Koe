package com.hazard.koe.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.data.model.Transaction
import com.hazard.koe.data.model.relations.TransactionWithDetails
import com.hazard.koe.domain.usecase.account.GetTotalAccountBalanceUseCase
import com.hazard.koe.domain.usecase.transaction.DeleteTransactionUseCase
import com.hazard.koe.domain.usecase.transaction.GetTotalByTypeInPeriodUseCase
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

data class HomeUiState(
    val dayGroups: List<DayGroup> = emptyList(),
    val isLoading: Boolean = true,
    val dateFilterMode: DateFilterMode = DateFilterMode.Month,
    val showDateFilterDialog: Boolean = false,
    val expense: Double = 0.0,
    val income: Double = 0.0,
    val totalAccountBalance: Double = 0.0
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    getTransactionsByDateRange: GetTransactionsByDateRangeUseCase,
    getTotalByTypeInPeriod: GetTotalByTypeInPeriodUseCase,
    getTotalAccountBalance: GetTotalAccountBalanceUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val _dateFilterMode = MutableStateFlow<DateFilterMode>(DateFilterMode.Month)
    private val _showDateFilterDialog = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = _dateFilterMode
        .flatMapLatest { mode ->
            val (start, end) = mode.dateRange()
            combine(
                getTransactionsByDateRange(start, end),
                getTotalByTypeInPeriod(TransactionType.EXPENSE, start, end),
                getTotalByTypeInPeriod(TransactionType.INCOME, start, end),
                getTotalAccountBalance(),
                _showDateFilterDialog
            ) { transactions, expenseTotal, incomeTotal, totalBalance, showDialog ->
                val groups = transactions
                    .groupBy { CurrencyFormatter.toLocalDate(it.transaction.date) }
                    .entries
                    .sortedByDescending { it.key }
                    .map { (date, txns) -> DayGroup(date, txns) }
                HomeUiState(
                    dayGroups = groups,
                    isLoading = false,
                    dateFilterMode = mode,
                    showDateFilterDialog = showDialog,
                    expense = expenseTotal / 100.0,
                    income = incomeTotal / 100.0,
                    totalAccountBalance = totalBalance / 100.0
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
    }

    fun onToggleDateFilterDialog() {
        _showDateFilterDialog.value = !_showDateFilterDialog.value
    }

    fun onDismissDateFilterDialog() {
        _showDateFilterDialog.value = false
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            deleteTransactionUseCase(transaction)
        }
    }
}
