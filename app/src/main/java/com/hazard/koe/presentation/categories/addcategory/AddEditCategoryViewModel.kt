package com.hazard.koe.presentation.categories.addcategory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazard.koe.data.enums.CategoryType
import com.hazard.koe.data.model.Category
import com.hazard.koe.domain.usecase.category.CreateCategoryUseCase
import com.hazard.koe.domain.usecase.category.GetCategoryByIdUseCase
import com.hazard.koe.domain.usecase.category.UpdateCategoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddEditCategoryViewModel(
    private val categoryId: Long?,
    private val createCategory: CreateCategoryUseCase,
    private val updateCategory: UpdateCategoryUseCase,
    private val getCategoryById: GetCategoryByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditCategoryUiState())
    val uiState: StateFlow<AddEditCategoryUiState> = _uiState.asStateFlow()

    init {
        if (categoryId != null) {
            loadCategory(categoryId)
        }
    }

    private fun loadCategory(id: Long) {
        viewModelScope.launch {
            getCategoryById(id).collect { category ->
                if (category != null) {
                    _uiState.update {
                        it.copy(
                            isEditMode = true,
                            categoryId = category.id,
                            emoji = category.emoji,
                            name = category.name,
                            categoryType = category.type,
                            color = category.color,
                            isSystem = category.isSystem
                        )
                    }
                }
            }
        }
    }

    fun updateEmoji(emoji: String) {
        _uiState.update { it.copy(emoji = emoji) }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun updateType(type: CategoryType) {
        _uiState.update { it.copy(categoryType = type) }
    }

    fun updateColor(color: String) {
        _uiState.update { it.copy(color = color) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Category name is required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                val category = Category(
                    id = state.categoryId ?: 0,
                    name = state.name.trim(),
                    emoji = state.emoji,
                    color = state.color,
                    type = state.categoryType,
                    isSystem = state.isSystem
                )
                if (state.isEditMode) updateCategory(category)
                else createCategory(category)
                _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = e.message) }
            }
        }
    }

    fun resetSubmitSuccess() {
        _uiState.update { it.copy(submitSuccess = false) }
    }
}
