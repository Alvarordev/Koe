package com.hazard.koe.domain.usecase.subscription

import com.hazard.koe.data.model.UserSubscription
import com.hazard.koe.domain.repository.UserSubscriptionRepository

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
