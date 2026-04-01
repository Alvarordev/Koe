package com.hazard.koe.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hazard.koe.data.model.ProcessedNotification

@Dao
interface ProcessedNotificationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notification: ProcessedNotification): Long

    @Query("SELECT EXISTS(SELECT 1 FROM processed_notifications WHERE dedupKey = :key)")
    suspend fun existsByDedupKey(key: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM processed_notifications WHERE operationNumber = :opNumber)")
    suspend fun existsByOperationNumber(opNumber: String): Boolean
}
