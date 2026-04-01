package com.hazard.koe.domain.usecase.transaction

import com.hazard.koe.domain.repository.TransactionRepository

class GetExpensesByCategoryUseCase(private val repository: TransactionRepository) {
    operator fun invoke(start: Long, end: Long) = repository.getExpensesByCategoryInPeriod(start, end)
}
