package com.example.tracker.domain.usecase.subscription

import com.example.tracker.domain.repository.SubscriptionServiceRepository

class GetSubscriptionServicesUseCase(private val repository: SubscriptionServiceRepository) {
    operator fun invoke() = repository.getAll()
}
