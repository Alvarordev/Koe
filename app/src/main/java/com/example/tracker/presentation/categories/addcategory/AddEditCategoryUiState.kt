package com.example.tracker.presentation.categories.addcategory

import com.example.tracker.data.enums.CategoryType

data class AddEditCategoryUiState(
    val isEditMode: Boolean = false,
    val categoryId: Long? = null,
    val emoji: String = "\uD83D\uDCE6",
    val name: String = "",
    val categoryType: CategoryType = CategoryType.EXPENSE,
    val color: String = "#1A73E8",
    val isSystem: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null
)
