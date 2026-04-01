package com.hazard.koe.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hazard.koe.data.model.CasualLoanTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CasualLoanTransactionDao {

    @Query("SELECT * FROM casual_loan_transactions WHERE casualLoanId = :loanId ORDER BY date DESC")
    fun getByLoan(loanId: Long): Flow<List<CasualLoanTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(txn: CasualLoanTransaction): Long
}
