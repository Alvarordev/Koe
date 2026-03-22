package com.example.tracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tracker.data.model.FormalLoan
import kotlinx.coroutines.flow.Flow

@Dao
interface FormalLoanDao {

    @Query("SELECT * FROM formal_loans ORDER BY createdAt DESC")
    fun getAll(): Flow<List<FormalLoan>>

    @Query("SELECT * FROM formal_loans WHERE isActive = 1")
    fun getActive(): Flow<List<FormalLoan>>

    @Query("SELECT * FROM formal_loans WHERE id = :id")
    fun getById(id: Long): Flow<FormalLoan?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loan: FormalLoan): Long

    @Update
    suspend fun update(loan: FormalLoan)
}
