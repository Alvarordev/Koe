package com.hazard.koe.presentation.categories

import com.hazard.koe.data.model.Category
import com.hazard.koe.data.model.relations.CategoryIdSummary
import com.hazard.koe.data.model.relations.SubscriptionWithDetails

data class CategoriesUiState(
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val categorySummaries: Map<Long, CategoryIdSummary> = emptyMap(),
    val subscriptions: List<SubscriptionWithDetails> = emptyList(),
    val isLoading: Boolean = true
)
