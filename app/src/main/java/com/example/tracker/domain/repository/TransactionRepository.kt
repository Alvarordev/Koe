package com.example.tracker.domain.repository

import com.example.tracker.data.enums.TransactionType
import com.example.tracker.data.model.Transaction
import com.example.tracker.data.model.relations.CategorySummary
import com.example.tracker.data.model.relations.CategoryTotal
import com.example.tracker.data.model.relations.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAll(): Flow<List<TransactionWithDetails>>
    fun getById(id: Long): Flow<TransactionWithDetails?>
    fun getByAccount(accountId: Long): Flow<List<TransactionWithDetails>>
    fun getByCategory(categoryId: Long): Flow<List<TransactionWithDetails>>
    fun getByDateRange(start: Long, end: Long): Flow<List<TransactionWithDetails>>
    fun getCategorySummaryInPeriod(categoryId: Long, start: Long, end: Long): Flow<CategorySummary>
    fun getExpensesByCategoryInPeriod(start: Long, end: Long): Flow<List<CategoryTotal>>
    fun getTotalByTypeInPeriod(type: TransactionType, start: Long, end: Long): Flow<Long>
    suspend fun create(transaction: Transaction): Long
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
}
