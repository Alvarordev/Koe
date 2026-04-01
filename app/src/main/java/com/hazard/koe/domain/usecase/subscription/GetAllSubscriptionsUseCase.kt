package com.hazard.koe.domain.usecase.subscription

import com.hazard.koe.domain.repository.UserSubscriptionRepository

class GetAllSubscriptionsUseCase(private val repository: UserSubscriptionRepository) {
    operator fun invoke() = repository.getAll()
}
