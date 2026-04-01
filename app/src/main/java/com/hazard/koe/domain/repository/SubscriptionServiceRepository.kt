package com.hazard.koe.domain.repository

import com.hazard.koe.data.model.SubscriptionService
import kotlinx.coroutines.flow.Flow

interface SubscriptionServiceRepository {
    fun getAll(): Flow<List<SubscriptionService>>
    fun searchByName(query: String): Flow<List<SubscriptionService>>
    suspend fun create(service: SubscriptionService): Long
    suspend fun seedServices()
}
