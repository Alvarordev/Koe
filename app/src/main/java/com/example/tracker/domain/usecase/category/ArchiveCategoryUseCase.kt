package com.example.tracker.domain.usecase.category

import com.example.tracker.domain.repository.CategoryRepository

class ArchiveCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(id: Long) = repository.archive(id)
}
