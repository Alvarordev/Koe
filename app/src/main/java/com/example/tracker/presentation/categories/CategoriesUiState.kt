package com.example.tracker.presentation.categories

import com.example.tracker.data.model.Category
import com.example.tracker.data.model.SubscriptionService

data class CategoriesUiState(
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val subscriptionServices: List<SubscriptionService> = emptyList(),
    val isLoading: Boolean = true
)

enum class CategoriesSubTab { CATEGORIES, SUBSCRIPTIONS }
