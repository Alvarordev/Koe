package com.example.tracker.data.repository

import com.example.tracker.data.model.SubscriptionService
import kotlinx.coroutines.flow.Flow

interface SubscriptionServiceRepository {
    fun getAll(): Flow<List<SubscriptionService>>
    fun searchByName(query: String): Flow<List<SubscriptionService>>
    suspend fun create(service: SubscriptionService): Long
    suspend fun seedServices()
}
