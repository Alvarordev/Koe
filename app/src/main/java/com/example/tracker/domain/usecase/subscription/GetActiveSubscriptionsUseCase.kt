package com.example.tracker.domain.usecase.subscription

import com.example.tracker.domain.repository.UserSubscriptionRepository

class GetActiveSubscriptionsUseCase(private val repository: UserSubscriptionRepository) {
    operator fun invoke() = repository.getActive()
}
