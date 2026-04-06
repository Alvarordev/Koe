package com.hazard.koe.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.hazard.koe.data.model.Budget
import com.hazard.koe.data.model.relations.BudgetWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Transaction
    @Query("SELECT * FROM budgets WHERE isActive = 1")
    fun getAll(): Flow<List<BudgetWithCategory>>

    @Transaction
    @Query("SELECT * FROM budgets WHERE id = :id")
    fun getById(id: Long): Flow<BudgetWithCategory?>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND isActive = 1 LIMIT 1")
    fun getByCategory(categoryId: Long): Flow<Budget?>

    @Query("SELECT * FROM budgets")
    suspend fun getAllRaw(): List<Budget>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<Budget>)

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)
}
