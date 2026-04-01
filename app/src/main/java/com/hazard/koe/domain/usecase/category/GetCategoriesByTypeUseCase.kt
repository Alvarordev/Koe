package com.hazard.koe.domain.usecase.category

import com.hazard.koe.data.enums.CategoryType
import com.hazard.koe.domain.repository.CategoryRepository

class GetCategoriesByTypeUseCase(private val repository: CategoryRepository) {
    operator fun invoke(type: CategoryType) = repository.getByType(type)
}
