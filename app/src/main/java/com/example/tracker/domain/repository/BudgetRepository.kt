package com.example.tracker.domain.repository

import com.example.tracker.data.model.Budget
import com.example.tracker.data.model.relations.BudgetWithCategory
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getAll(): Flow<List<BudgetWithCategory>>
    fun getById(id: Long): Flow<BudgetWithCategory?>
    fun getByCategory(categoryId: Long): Flow<Budget?>
    suspend fun create(budget: Budget): Long
    suspend fun update(budget: Budget)
    suspend fun delete(budget: Budget)
}
