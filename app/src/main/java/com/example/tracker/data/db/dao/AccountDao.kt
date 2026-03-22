package com.example.tracker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tracker.data.enums.AccountType
import com.example.tracker.data.model.Account
import com.example.tracker.data.model.relations.CurrencyBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE isArchived = 0 ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getById(id: Long): Flow<Account?>

    @Query("SELECT * FROM accounts WHERE type = :type AND isArchived = 0")
    fun getByType(type: AccountType): Flow<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: Account): Long

    @Update
    suspend fun update(account: Account)

    @Query("UPDATE accounts SET currentBalance = :newBalance, updatedAt = :now WHERE id = :id")
    suspend fun updateBalance(id: Long, newBalance: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE accounts SET creditUsed = :creditUsed, updatedAt = :now WHERE id = :id")
    suspend fun updateCreditUsed(id: Long, creditUsed: Long, now: Long = System.currentTimeMillis())

    @Query("UPDATE accounts SET isArchived = 1, updatedAt = :now WHERE id = :id")
    suspend fun archive(id: Long, now: Long = System.currentTimeMillis())

    @Query("SELECT SUM(currentBalance) FROM accounts WHERE isArchived = 0")
    fun getTotalBalance(): Flow<Long>

    @Query("SELECT currencyCode, SUM(currentBalance) as totalBalance FROM accounts WHERE isArchived = 0 GROUP BY currencyCode")
    fun getTotalBalanceByCurrency(): Flow<List<CurrencyBalance>>
}
