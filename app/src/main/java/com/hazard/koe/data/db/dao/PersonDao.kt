package com.hazard.koe.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hazard.koe.data.model.Person
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Query("SELECT * FROM persons ORDER BY name ASC")
    fun getAll(): Flow<List<Person>>

    @Query("SELECT * FROM persons WHERE id = :id")
    fun getById(id: Long): Flow<Person?>

    @Query("SELECT * FROM persons WHERE name LIKE '%' || :query || '%'")
    fun searchByName(query: String): Flow<List<Person>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(person: Person): Long

    @Update
    suspend fun update(person: Person)
}
