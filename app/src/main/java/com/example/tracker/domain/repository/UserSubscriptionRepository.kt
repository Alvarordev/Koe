package com.example.tracker.domain.repository

import com.example.tracker.data.model.UserSubscription
import com.example.tracker.data.model.relations.SubscriptionWithDetails
import kotlinx.coroutines.flow.Flow

interface UserSubscriptionRepository {
    fun getAll(): Flow<List<SubscriptionWithDetails>>
    fun getActive(): Flow<List<SubscriptionWithDetails>>
    suspend fun insert(subscription: UserSubscription): Long
    suspend fun update(subscription: UserSubscription)
    suspend fun deleteById(id: Long)
}
