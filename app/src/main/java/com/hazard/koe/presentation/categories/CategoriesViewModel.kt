package com.hazard.koe.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazard.koe.data.enums.CategoryType
import com.hazard.koe.domain.usecase.category.ArchiveCategoryUseCase
import com.hazard.koe.domain.usecase.category.GetCategoriesUseCase
import com.hazard.koe.domain.usecase.subscription.GetActiveSubscriptionsUseCase
import com.hazard.koe.domain.usecase.transaction.GetAllCategorySummariesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class CategoriesViewModel(
    getCategories: GetCategoriesUseCase,
    getActiveSubscriptions: GetActiveSubscriptionsUseCase,
    getAllCategorySummaries: GetAllCategorySummariesUseCase,
    private val archiveCategoryUseCase: ArchiveCategoryUseCase
) : ViewModel() {

    private val now = LocalDate.now()
    private val monthStart = now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val monthEnd = now.plusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val uiState: StateFlow<CategoriesUiState> = combine(
        getCategories(),
        getActiveSubscriptions(),
        getAllCategorySummaries(monthStart, monthEnd)
    ) { categories, subscriptions, summaries ->
        val summaryMap = summaries.associateBy { it.categoryId }
        CategoriesUiState(
            expenseCategories = categories
                .filter { it.type == CategoryType.EXPENSE && !it.isArchived }
                .sortedBy { it.sortOrder },
            incomeCategories = categories
                .filter { it.type == CategoryType.INCOME && !it.isArchived }
                .sortedBy { it.sortOrder },
            categorySummaries = summaryMap,
            subscriptions = subscriptions,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoriesUiState()
    )

    fun archiveCategory(id: Long) {
        viewModelScope.launch { archiveCategoryUseCase(id) }
    }
}
