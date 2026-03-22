package com.example.tracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tracker.data.model.SubscriptionService
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionServiceDao {

    @Query("SELECT * FROM subscription_services ORDER BY name ASC")
    fun getAll(): Flow<List<SubscriptionService>>

    @Query("SELECT * FROM subscription_services WHERE name LIKE '%' || :query || '%'")
    fun searchByName(query: String): Flow<List<SubscriptionService>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(service: SubscriptionService): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(services: List<SubscriptionService>)
}
