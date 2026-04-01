package com.hazard.koe.domain.repository

import com.hazard.koe.data.model.Person
import kotlinx.coroutines.flow.Flow

interface PersonRepository {
    fun getAll(): Flow<List<Person>>
    fun getById(id: Long): Flow<Person?>
    fun searchByName(query: String): Flow<List<Person>>
    suspend fun create(person: Person): Long
    suspend fun update(person: Person)
}
