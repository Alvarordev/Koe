package com.example.tracker.domain.usecase.recurring

import com.example.tracker.domain.repository.RecurringRuleRepository

class GetRecurringRulesUseCase(private val repository: RecurringRuleRepository) {
    operator fun invoke() = repository.getAll()
}
