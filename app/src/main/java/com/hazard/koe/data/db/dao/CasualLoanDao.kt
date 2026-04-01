package com.hazard.koe.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.hazard.koe.data.model.CasualLoan
import com.hazard.koe.data.model.relations.CasualLoanWithPerson
import com.hazard.koe.data.model.relations.PersonLoanSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface CasualLoanDao {

    @Transaction
    @Query("SELECT * FROM casual_loans ORDER BY createdAt DESC")
    fun getAll(): Flow<List<CasualLoanWithPerson>>

    @Query("SELECT * FROM casual_loans WHERE personId = :personId")
    fun getByPerson(personId: Long): Flow<List<CasualLoan>>

    @Transaction
    @Query("SELECT * FROM casual_loans WHERE isPaidOff = 0")
    fun getActive(): Flow<List<CasualLoanWithPerson>>

    @Query("""
        SELECT cl.personId, p.name as personName, cl.direction, SUM(cl.outstandingBalance) as totalOutstanding
        FROM casual_loans cl
        INNER JOIN persons p ON cl.personId = p.id
        WHERE cl.isPaidOff = 0
        GROUP BY cl.personId, cl.direction
    """)
    fun getSummaryByPerson(): Flow<List<PersonLoanSummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(loan: CasualLoan): Long

    @Update
    suspend fun update(loan: CasualLoan)

    @Query("UPDATE casual_loans SET isPaidOff = 1, outstandingBalance = 0, updatedAt = :now WHERE id = :id")
    suspend fun markPaidOff(id: Long, now: Long = System.currentTimeMillis())
}
