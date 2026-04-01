package com.hazard.koe.domain.usecase.category

import com.hazard.koe.domain.repository.CategoryRepository

class ArchiveCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: Long) = repository.archive(id)
}
