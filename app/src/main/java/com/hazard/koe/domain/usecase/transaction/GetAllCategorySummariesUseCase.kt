package com.hazard.koe.domain.usecase.transaction

import com.hazard.koe.domain.repository.TransactionRepository

class GetAllCategorySummariesUseCase(private val repository: TransactionRepository) {
    operator fun invoke(start: Long, end: Long) = repository.getAllCategorySummariesInPeriod(start, end)
}
