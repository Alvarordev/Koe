package com.hazard.koe.domain.usecase.subscription

import com.hazard.koe.domain.repository.SubscriptionServiceRepository

class GetSubscriptionServicesUseCase(private val repository: SubscriptionServiceRepository) {
    operator fun invoke() = repository.getAll()
}
