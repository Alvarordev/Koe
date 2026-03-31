package com.example.tracker.domain.usecase.subscription

import com.example.tracker.data.model.UserSubscription
import com.example.tracker.domain.repository.UserSubscriptionRepository

class SaveSubscriptionUseCase(private val repository: UserSubscriptionRepository) {
    suspend operator fun invoke(sub: UserSubscription): Long {
        return if (sub.id == 0L) {
            repository.insert(sub)
        } else {
            repository.update(sub)
            sub.id
        }
    }
}
