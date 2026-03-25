package com.example.tracker.presentation.addtransaction

import com.example.tracker.data.model.Account
import com.example.tracker.data.model.Category
import com.example.tracker.data.model.relations.CategorySummary

data class AddTransactionUiState(
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val selectedCategory: Category? = null,
    val selectedAccount: Account? = null,
    val amountString: String = "",
    val description: String = "",
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitSuccess: Boolean = false,
    val isLocationEnabled: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isProcessingYapeImage: Boolean = false,
    val yapeOperationNumber: String? = null,
    val prefilledSource: String? = null,
    val selectedDate: Long = System.currentTimeMillis(),
    val categorySummary: CategorySummary? = null,
    val categorySummaries: Map<Long, CategorySummary> = emptyMap()
)
