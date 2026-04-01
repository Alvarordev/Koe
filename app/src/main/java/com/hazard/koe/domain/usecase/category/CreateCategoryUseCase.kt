package com.hazard.koe.domain.usecase.category

import com.hazard.koe.data.model.Category
import com.hazard.koe.domain.repository.CategoryRepository

class CreateCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(category: Category): Long = repository.create(category)
}
