package com.example.tracker.data.repository.impl

import com.example.tracker.data.db.dao.UserSubscriptionDao
import com.example.tracker.data.model.UserSubscription
import com.example.tracker.data.model.relations.SubscriptionWithDetails
import com.example.tracker.domain.repository.UserSubscriptionRepository
import kotlinx.coroutines.flow.Flow

class UserSubscriptionRepositoryImpl(
    private val dao: UserSubscriptionDao
) : UserSubscriptionRepository {

    override fun getAll(): Flow<List<SubscriptionWithDetails>> = dao.getAll()

    override fun getActive(): Flow<List<SubscriptionWithDetails>> = dao.getActive()

    override suspend fun insert(subscription: UserSubscription): Long = dao.insert(subscription)

    override suspend fun update(subscription: UserSubscription) = dao.update(subscription)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
