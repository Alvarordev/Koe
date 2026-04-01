package com.hazard.koe.domain.usecase.transaction

import com.hazard.koe.data.model.relations.CategorySummary
import com.hazard.koe.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetCategorySummaryUseCase(private val repository: TransactionRepository) {
    operator fun invoke(categoryId: Long, start: Long, end: Long): Flow<CategorySummary> =
        repository.getCategorySummaryInPeriod(categoryId, start, end)
}
