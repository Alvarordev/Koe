package com.example.tracker.data.repository.impl

import com.example.tracker.data.db.dao.SubscriptionServiceDao
import com.example.tracker.data.model.SubscriptionService
import com.example.tracker.domain.repository.SubscriptionServiceRepository
import kotlinx.coroutines.flow.Flow

class SubscriptionServiceRepositoryImpl(private val dao: SubscriptionServiceDao) : SubscriptionServiceRepository {

    override fun getAll(): Flow<List<SubscriptionService>> = dao.getAll()

    override fun searchByName(query: String): Flow<List<SubscriptionService>> = dao.searchByName(query)

    override suspend fun create(service: SubscriptionService): Long = dao.insert(service)

    override suspend fun seedServices() {
        // Seeded via DatabaseSeeder on DB creation. No-op here.
    }
}
