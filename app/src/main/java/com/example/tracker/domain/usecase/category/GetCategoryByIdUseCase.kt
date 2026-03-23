package com.example.tracker.domain.usecase.category

import com.example.tracker.domain.repository.CategoryRepository

class GetCategoryByIdUseCase(private val repository: CategoryRepository) {
    operator fun invoke(id: Long) = repository.getById(id)
}
