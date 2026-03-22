package com.example.tracker.domain.usecase.category

import com.example.tracker.domain.repository.CategoryRepository

class GetCategoriesUseCase(private val repository: CategoryRepository) {
    operator fun invoke() = repository.getAll()
}
