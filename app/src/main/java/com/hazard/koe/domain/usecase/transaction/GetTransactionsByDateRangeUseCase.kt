package com.hazard.koe.domain.usecase.transaction

import com.hazard.koe.domain.repository.TransactionRepository

class GetTransactionsByDateRangeUseCase(private val repository: TransactionRepository) {
    operator fun invoke(start: Long, end: Long) = repository.getByDateRange(start, end)
}
