package com.hazard.koe.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.hazard.koe.data.enums.RecurringType
import com.hazard.koe.data.model.RecurringRule
import com.hazard.koe.data.model.relations.RecurringRuleWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringRuleDao {

    @Transaction
    @Query("SELECT * FROM recurring_rules WHERE isActive = 1")
    fun getAll(): Flow<List<RecurringRuleWithDetails>>

    @Transaction
    @Query("SELECT * FROM recurring_rules WHERE type = :type AND isActive = 1")
    fun getByType(type: RecurringType): Flow<List<RecurringRuleWithDetails>>

    @Query("SELECT * FROM recurring_rules WHERE nextOccurrence <= :beforeDate AND isActive = 1")
    suspend fun getDueRules(beforeDate: Long): List<RecurringRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: RecurringRule): Long

    @Update
    suspend fun update(rule: RecurringRule)

    @Query("UPDATE recurring_rules SET nextOccurrence = :next, updatedAt = :now WHERE id = :id")
    suspend fun updateNextOccurrence(id: Long, next: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE recurring_rules SET isActive = 0, updatedAt = :now WHERE id = :id")
    suspend fun deactivate(id: Long, now: Long = System.currentTimeMillis())
}
