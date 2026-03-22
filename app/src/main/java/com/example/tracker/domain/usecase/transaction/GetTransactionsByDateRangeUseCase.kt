package com.example.tracker.domain.usecase.transaction

import com.example.tracker.domain.repository.TransactionRepository

class GetTransactionsByDateRangeUseCase(private val repository: TransactionRepository) {
    operator fun invoke(start: Long, end: Long) = repository.getByDateRange(start, end)
}
