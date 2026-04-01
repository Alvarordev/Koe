package com.hazard.koe.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.hazard.koe.data.model.UserSubscription
import com.hazard.koe.data.model.relations.SubscriptionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSubscriptionDao {

    @Transaction
    @Query("SELECT * FROM user_subscriptions WHERE isArchived = 0 ORDER BY billingDay ASC")
    fun getActive(): Flow<List<SubscriptionWithDetails>>

    @Transaction
    @Query("SELECT * FROM user_subscriptions ORDER BY billingDay ASC")
    fun getAll(): Flow<List<SubscriptionWithDetails>>

    @Query("SELECT * FROM user_subscriptions WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<UserSubscription?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sub: UserSubscription): Long

    @Update
    suspend fun update(sub: UserSubscription)

    @Query("DELETE FROM user_subscriptions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
