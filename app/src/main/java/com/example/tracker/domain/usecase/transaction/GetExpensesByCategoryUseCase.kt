package com.example.tracker.domain.usecase.transaction

import com.example.tracker.domain.repository.TransactionRepository

class GetExpensesByCategoryUseCase(private val repository: TransactionRepository) {
    operator fun invoke(start: Long, end: Long) = repository.getExpensesByCategoryInPeriod(start, end)
}
