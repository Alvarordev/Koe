package com.hazard.koe.data.repository.impl

import com.hazard.koe.data.db.dao.PersonDao
import com.hazard.koe.data.model.Person
import com.hazard.koe.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow

class PersonRepositoryImpl(private val dao: PersonDao) : PersonRepository {

    override fun getAll(): Flow<List<Person>> = dao.getAll()

    override fun getById(id: Long): Flow<Person?> = dao.getById(id)

    override fun searchByName(query: String): Flow<List<Person>> = dao.searchByName(query)

    override suspend fun create(person: Person): Long = dao.insert(person)

    override suspend fun update(person: Person) = dao.update(person)
}
