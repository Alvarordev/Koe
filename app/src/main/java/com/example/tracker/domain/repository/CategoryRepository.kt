package com.example.tracker.domain.repository

import com.example.tracker.data.enums.CategoryType
import com.example.tracker.data.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAll(): Flow<List<Category>>
    fun getByType(type: CategoryType): Flow<List<Category>>
    fun getById(id: Long): Flow<Category?>
    fun getSystemCategories(): Flow<List<Category>>
    suspend fun create(category: Category): Long
    suspend fun update(category: Category)
    suspend fun archive(id: Long)
    suspend fun seedSystemCategories()
    suspend fun getTransferCategory(): Category?
}
