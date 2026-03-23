package com.example.tracker.domain.usecase.transaction

import com.example.tracker.data.model.relations.CategorySummary
import com.example.tracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetCategorySummaryUseCase(private val repository: TransactionRepository) {
    operator fun invoke(categoryId: Long, start: Long, end: Long): Flow<CategorySummary> =
        repository.getCategorySummaryInPeriod(categoryId, start, end)
}
