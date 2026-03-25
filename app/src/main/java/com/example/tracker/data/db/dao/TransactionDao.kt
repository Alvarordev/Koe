package com.example.tracker.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction as RoomTransaction
import androidx.room.Update
import com.example.tracker.data.enums.TransactionType
import com.example.tracker.data.model.Transaction
import com.example.tracker.data.model.relations.CategoryIdSummary
import com.example.tracker.data.model.relations.CategorySummary
import com.example.tracker.data.model.relations.CategoryTotal
import com.example.tracker.data.model.relations.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @RoomTransaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<TransactionWithDetails>>

    @RoomTransaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getById(id: Long): Flow<TransactionWithDetails?>

    @RoomTransaction
    @Query("SELECT * FROM transactions WHERE accountId = :accountId OR transferToAccountId = :accountId ORDER BY date DESC")
    fun getByAccount(accountId: Long): Flow<List<TransactionWithDetails>>

    @RoomTransaction
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getByCategory(categoryId: Long): Flow<List<TransactionWithDetails>>

    @RoomTransaction
    @Query("SELECT * FROM transactions WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getByDateRange(start: Long, end: Long): Flow<List<TransactionWithDetails>>

    @RoomTransaction
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getByType(type: TransactionType): Flow<List<TransactionWithDetails>>

    @Query("""
        SELECT t.categoryId, c.name as categoryName, c.emoji, c.color, SUM(t.amount) as total
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.type = 'EXPENSE' AND t.date BETWEEN :start AND :end
        GROUP BY t.categoryId
        ORDER BY total DESC
    """)
    fun getExpensesByCategoryInPeriod(start: Long, end: Long): Flow<List<CategoryTotal>>

    @Query("""
        SELECT COUNT(id) as count, COALESCE(SUM(amount), 0) as total
        FROM transactions
        WHERE categoryId = :categoryId AND date BETWEEN :start AND :end
    """)
    fun getCategorySummaryInPeriod(categoryId: Long, start: Long, end: Long): Flow<CategorySummary>

    @Query("""
        SELECT categoryId, COUNT(id) as count, COALESCE(SUM(amount), 0) as total
        FROM transactions
        WHERE date BETWEEN :start AND :end
        GROUP BY categoryId
    """)
    fun getAllCategorySummariesInPeriod(start: Long, end: Long): Flow<List<CategoryIdSummary>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND date BETWEEN :start AND :end")
    fun getTotalByTypeInPeriod(type: TransactionType, start: Long, end: Long): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)
}
