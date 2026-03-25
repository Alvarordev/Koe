package com.example.tracker.domain.usecase.transaction

import com.example.tracker.data.enums.TransactionType
import com.example.tracker.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTotalByTypeInPeriodUseCase(private val repository: TransactionRepository) {
    operator fun invoke(type: TransactionType, start: Long, end: Long): Flow<Long> =
        repository.getTotalByTypeInPeriod(type, start, end)
}
