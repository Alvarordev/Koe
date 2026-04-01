package com.hazard.koe.domain.repository

import com.hazard.koe.data.model.Budget
import com.hazard.koe.data.model.relations.BudgetWithCategory
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getAll(): Flow<List<BudgetWithCategory>>
    fun getById(id: Long): Flow<BudgetWithCategory?>
    fun getByCategory(categoryId: Long): Flow<Budget?>
    suspend fun create(budget: Budget): Long
    suspend fun update(budget: Budget)
    suspend fun delete(budget: Budget)
}
