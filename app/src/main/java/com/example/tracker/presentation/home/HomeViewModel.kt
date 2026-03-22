package com.example.tracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.model.relations.TransactionWithDetails
import com.example.tracker.domain.usecase.transaction.GetTransactionsUseCase
import com.example.tracker.presentation.util.CurrencyFormatter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class DayGroup(
    val date: LocalDate,
    val transactions: List<TransactionWithDetails>
)

data class HomeUiState(
    val dayGroups: List<DayGroup> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(
    getTransactions: GetTransactionsUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getTransactions()
        .map { transactions ->
            val groups = transactions
                .groupBy { CurrencyFormatter.toLocalDate(it.transaction.date) }
                .entries
                .sortedByDescending { it.key }
                .map { (date, txns) -> DayGroup(date, txns) }
            HomeUiState(dayGroups = groups, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )
}
