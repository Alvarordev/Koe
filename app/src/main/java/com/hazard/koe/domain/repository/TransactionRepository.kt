package com.hazard.koe.domain.repository

import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.data.model.Transaction
import com.hazard.koe.data.model.relations.CategoryIdSummary
import com.hazard.koe.data.model.relations.CategorySummary
import com.hazard.koe.data.model.relations.CategoryTotal
import com.hazard.koe.data.model.relations.TransactionWithDetails
import com.hazard.koe.data.model.relations.TransactionWithMapData
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAll(): Flow<List<TransactionWithDetails>>
    fun getById(id: Long): Flow<TransactionWithDetails?>
    fun getByAccount(accountId: Long): Flow<List<TransactionWithDetails>>
    fun getByCategory(categoryId: Long): Flow<List<TransactionWithDetails>>
    fun getByDateRange(start: Long, end: Long): Flow<List<TransactionWithDetails>>
    fun getCategorySummaryInPeriod(categoryId: Long, start: Long, end: Long): Flow<CategorySummary>
    fun getAllCategorySummariesInPeriod(start: Long, end: Long): Flow<List<CategoryIdSummary>>
    fun getExpensesByCategoryInPeriod(start: Long, end: Long): Flow<List<CategoryTotal>>
    fun getTotalByTypeInPeriod(type: TransactionType, start: Long, end: Long): Flow<Long>
    suspend fun create(transaction: Transaction): Long
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
    suspend fun getLastBySubscriptionId(subscriptionId: Long): Transaction?
    suspend fun deleteFutureBySubscriptionId(subscriptionId: Long, afterDate: Long)
    fun getTransactionsWithCoordinatesByMonth(startMs: Long, endMs: Long): Flow<List<TransactionWithMapData>>
}
