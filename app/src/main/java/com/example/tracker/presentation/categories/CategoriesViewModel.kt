package com.example.tracker.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.enums.CategoryType
import com.example.tracker.domain.usecase.category.ArchiveCategoryUseCase
import com.example.tracker.domain.usecase.category.GetCategoriesUseCase
import com.example.tracker.domain.usecase.subscription.GetSubscriptionServicesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(
    getCategories: GetCategoriesUseCase,
    getSubscriptionServices: GetSubscriptionServicesUseCase,
    private val archiveCategoryUseCase: ArchiveCategoryUseCase
) : ViewModel() {

    val uiState: StateFlow<CategoriesUiState> = combine(
        getCategories(),
        getSubscriptionServices()
    ) { categories, subscriptions ->
        CategoriesUiState(
            expenseCategories = categories.filter { it.type == CategoryType.EXPENSE },
            incomeCategories = categories.filter { it.type == CategoryType.INCOME },
            subscriptionServices = subscriptions,
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
