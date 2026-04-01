package com.hazard.koe.domain.usecase.recurring

import com.hazard.koe.data.enums.RecurringType
import com.hazard.koe.domain.repository.RecurringRuleRepository

class GetSubscriptionRulesUseCase(private val repository: RecurringRuleRepository) {
    operator fun invoke() = repository.getByType(RecurringType.SUBSCRIPTION)
}
