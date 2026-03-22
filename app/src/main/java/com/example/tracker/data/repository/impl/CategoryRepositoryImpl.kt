package com.example.tracker.data.repository.impl

import com.example.tracker.data.db.dao.CategoryDao
import com.example.tracker.data.enums.CategoryType
import com.example.tracker.data.model.Category
import com.example.tracker.data.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class CategoryRepositoryImpl(private val dao: CategoryDao) : CategoryRepository {

    override fun getAll(): Flow<List<Category>> = dao.getAll()

    override fun getByType(type: CategoryType): Flow<List<Category>> = dao.getByType(type)

    override fun getById(id: Long): Flow<Category?> = dao.getById(id)

    override fun getSystemCategories(): Flow<List<Category>> = dao.getSystemCategories()

    override suspend fun create(category: Category): Long = dao.insert(category)

    override suspend fun update(category: Category) = dao.update(category)

    override suspend fun archive(id: Long) = dao.archive(id)

    override suspend fun seedSystemCategories() {
        // System categories are seeded via DatabaseSeeder on DB creation.
        // This method is a no-op for the Room implementation.
    }
}
