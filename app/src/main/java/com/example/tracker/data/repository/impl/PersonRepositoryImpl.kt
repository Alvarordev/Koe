package com.example.tracker.data.repository.impl

import com.example.tracker.data.db.dao.PersonDao
import com.example.tracker.data.model.Person
import com.example.tracker.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow

class PersonRepositoryImpl(private val dao: PersonDao) : PersonRepository {

    override fun getAll(): Flow<List<Person>> = dao.getAll()

    override fun getById(id: Long): Flow<Person?> = dao.getById(id)

    override fun searchByName(query: String): Flow<List<Person>> = dao.searchByName(query)

    override suspend fun create(person: Person): Long = dao.insert(person)

    override suspend fun update(person: Person) = dao.update(person)
}
