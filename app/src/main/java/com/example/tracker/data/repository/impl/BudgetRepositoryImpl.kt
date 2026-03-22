package com.example.tracker.data.repository.impl

import com.example.tracker.data.db.dao.BudgetDao
import com.example.tracker.data.model.Budget
import com.example.tracker.data.model.relations.BudgetWithCategory
import com.example.tracker.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow

class BudgetRepositoryImpl(private val dao: BudgetDao) : BudgetRepository {

    override fun getAll(): Flow<List<BudgetWithCategory>> = dao.getAll()

    override fun getById(id: Long): Flow<BudgetWithCategory?> = dao.getById(id)

    override fun getByCategory(categoryId: Long): Flow<Budget?> = dao.getByCategory(categoryId)

    override suspend fun create(budget: Budget): Long = dao.insert(budget)

    override suspend fun update(budget: Budget) = dao.update(budget)

    override suspend fun delete(budget: Budget) = dao.delete(budget)
}
