package com.example.tracker.domain.usecase.subscription

import com.example.tracker.domain.repository.UserSubscriptionRepository

class GetAllSubscriptionsUseCase(private val repository: UserSubscriptionRepository) {
    operator fun invoke() = repository.getAll()
}
