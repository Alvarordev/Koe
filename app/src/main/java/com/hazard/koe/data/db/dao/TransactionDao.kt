package com.hazard.koe.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction as RoomTransaction
import androidx.room.Update
import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.data.model.Transaction
import com.hazard.koe.data.model.relations.CategoryIdSummary
import com.hazard.koe.data.model.relations.CategorySummary
import com.hazard.koe.data.model.relations.CategoryTotal
import com.hazard.koe.data.model.relations.TransactionWithDetails
import com.hazard.koe.data.model.relations.TransactionWithMapData
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
    @Query("SELECT * FROM transactions WHERE accountId = :accountId OR transferToAccountId = :accountId ORDER BY date DESC LIMIT 20")
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

    @Query("SELECT * FROM transactions WHERE subscriptionId = :subscriptionId ORDER BY date DESC LIMIT 1")
    suspend fun getLastBySubscriptionId(subscriptionId: Long): Transaction?

    @Query("DELETE FROM transactions WHERE subscriptionId = :subscriptionId AND date > :afterDate")
    suspend fun deleteFutureBySubscriptionId(subscriptionId: Long, afterDate: Long)

    @Query("""
        SELECT t.id, t.amount, t.date, t.latitude, t.longitude,
               c.emoji AS categoryEmoji, c.color AS categoryColor, c.name AS categoryName
        FROM transactions t
        INNER JOIN categories c ON t.categoryId = c.id
        WHERE t.date >= :startMs AND t.date < :endMs
        AND t.latitude IS NOT NULL AND t.longitude IS NOT NULL
    """)
    fun getTransactionsWithCoordinatesByMonth(startMs: Long, endMs: Long): Flow<List<TransactionWithMapData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)
}
