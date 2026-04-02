package com.hazard.koe.domain.usecase.transaction

import com.hazard.koe.data.model.relations.TransactionWithMapData
import com.hazard.koe.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import java.time.ZoneId

class GetTransactionsWithCoordinatesByMonthUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(yearMonth: YearMonth): Flow<List<TransactionWithMapData>> {
        val zoneId = ZoneId.systemDefault()
        val startMs = yearMonth.atDay(1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
        val endMs = yearMonth.plusMonths(1).atDay(1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
        return repository.getTransactionsWithCoordinatesByMonth(startMs, endMs)
    }
}
