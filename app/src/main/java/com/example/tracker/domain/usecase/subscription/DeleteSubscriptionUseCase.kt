package com.example.tracker.domain.usecase.subscription

import com.example.tracker.domain.repository.UserSubscriptionRepository

class DeleteSubscriptionUseCase(private val repository: UserSubscriptionRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteById(id)
}
