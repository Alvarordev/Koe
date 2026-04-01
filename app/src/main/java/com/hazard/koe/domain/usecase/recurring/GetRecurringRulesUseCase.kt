package com.hazard.koe.domain.usecase.recurring

import com.hazard.koe.domain.repository.RecurringRuleRepository

class GetRecurringRulesUseCase(private val repository: RecurringRuleRepository) {
    operator fun invoke() = repository.getAll()
}
