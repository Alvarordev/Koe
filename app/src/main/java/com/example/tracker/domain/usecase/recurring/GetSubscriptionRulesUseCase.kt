package com.example.tracker.domain.usecase.recurring

import com.example.tracker.data.enums.RecurringType
import com.example.tracker.domain.repository.RecurringRuleRepository

class GetSubscriptionRulesUseCase(private val repository: RecurringRuleRepository) {
    operator fun invoke() = repository.getByType(RecurringType.SUBSCRIPTION)
}
