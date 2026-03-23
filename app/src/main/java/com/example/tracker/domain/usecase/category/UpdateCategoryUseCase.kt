package com.example.tracker.domain.usecase.category

import com.example.tracker.data.model.Category
import com.example.tracker.domain.repository.CategoryRepository

class UpdateCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(category: Category) = repository.update(category)
}
