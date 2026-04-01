package com.hazard.koe.data.repository.impl

import com.hazard.koe.data.db.dao.BudgetDao
import com.hazard.koe.data.model.Budget
import com.hazard.koe.data.model.relations.BudgetWithCategory
import com.hazard.koe.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow

class BudgetRepositoryImpl(private val dao: BudgetDao) : BudgetRepository {

    override fun getAll(): Flow<List<BudgetWithCategory>> = dao.getAll()

    override fun getById(id: Long): Flow<BudgetWithCategory?> = dao.getById(id)

    override fun getByCategory(categoryId: Long): Flow<Budget?> = dao.getByCategory(categoryId)

    override suspend fun create(budget: Budget): Long = dao.insert(budget)

    override suspend fun update(budget: Budget) = dao.update(budget)

    override suspend fun delete(budget: Budget) = dao.delete(budget)
}
