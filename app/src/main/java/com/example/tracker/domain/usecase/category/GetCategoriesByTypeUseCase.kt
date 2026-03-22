package com.example.tracker.domain.usecase.category

import com.example.tracker.data.enums.CategoryType
import com.example.tracker.domain.repository.CategoryRepository

class GetCategoriesByTypeUseCase(private val repository: CategoryRepository) {
    operator fun invoke(type: CategoryType) = repository.getByType(type)
}
