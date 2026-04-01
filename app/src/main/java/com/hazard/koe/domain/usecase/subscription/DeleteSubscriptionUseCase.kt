package com.hazard.koe.domain.usecase.subscription

import com.hazard.koe.domain.repository.UserSubscriptionRepository

class DeleteSubscriptionUseCase(private val repository: UserSubscriptionRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteById(id)
}
