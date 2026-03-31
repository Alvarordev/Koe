package com.example.tracker.data.repository.impl

import com.example.tracker.data.db.dao.CategoryDao
import com.example.tracker.data.enums.CategoryType
import com.example.tracker.data.model.Category
import com.example.tracker.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CategoryRepositoryImpl(private val dao: CategoryDao) : CategoryRepository {

    private val subscriptionCategoryMutex = Mutex()

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

    override suspend fun getTransferCategory(): Category? = dao.getTransferCategory()

    override suspend fun getOrCreateSubscriptionCategory(): Category {
        return subscriptionCategoryMutex.withLock {
            dao.getSubscriptionCategory() ?: run {
                val template = Category(
                    name = "Suscripciones",
                    emoji = "\uD83D\uDCC5",
                    color = "#6366F1",
                    type = CategoryType.EXPENSE,
                    isSystem = true,
                    isArchived = true,
                    sortOrder = 998
                )
                val id = dao.insert(template)
                template.copy(id = id)
            }
        }
    }
}
