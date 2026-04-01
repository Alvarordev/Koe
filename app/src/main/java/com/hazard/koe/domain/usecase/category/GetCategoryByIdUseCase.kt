package com.hazard.koe.domain.usecase.category

import com.hazard.koe.domain.repository.CategoryRepository

class GetCategoryByIdUseCase(private val repository: CategoryRepository) {
    operator fun invoke(id: Long) = repository.getById(id)
}
