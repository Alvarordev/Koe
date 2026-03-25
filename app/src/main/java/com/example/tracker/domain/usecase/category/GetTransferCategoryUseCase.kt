package com.example.tracker.domain.usecase.category

import com.example.tracker.data.model.Category
import com.example.tracker.domain.repository.CategoryRepository

class GetTransferCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(): Category? = repository.getTransferCategory()
}
