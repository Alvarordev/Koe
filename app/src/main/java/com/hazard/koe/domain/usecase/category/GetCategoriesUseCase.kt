package com.hazard.koe.domain.usecase.category

import com.hazard.koe.domain.repository.CategoryRepository

class GetCategoriesUseCase(private val repository: CategoryRepository) {
    operator fun invoke() = repository.getAll()
}
