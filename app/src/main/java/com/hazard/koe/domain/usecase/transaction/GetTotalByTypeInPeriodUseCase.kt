package com.hazard.koe.domain.usecase.transaction

import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

class GetTotalByTypeInPeriodUseCase(private val repository: TransactionRepository) {
    operator fun invoke(type: TransactionType, start: Long, end: Long): Flow<Long> =
        repository.getTotalByTypeInPeriod(type, start, end)
}
