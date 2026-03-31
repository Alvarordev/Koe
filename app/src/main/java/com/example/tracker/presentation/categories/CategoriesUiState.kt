package com.example.tracker.presentation.categories

import com.example.tracker.data.model.Category
import com.example.tracker.data.model.relations.CategoryIdSummary
import com.example.tracker.data.model.relations.SubscriptionWithDetails

data class CategoriesUiState(
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val categorySummaries: Map<Long, CategoryIdSummary> = emptyMap(),
    val subscriptions: List<SubscriptionWithDetails> = emptyList(),
    val isLoading: Boolean = true
)
