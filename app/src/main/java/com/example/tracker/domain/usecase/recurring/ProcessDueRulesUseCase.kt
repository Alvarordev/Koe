package com.example.tracker.domain.usecase.recurring

import com.example.tracker.domain.repository.RecurringRuleRepository

class ProcessDueRulesUseCase(private val repository: RecurringRuleRepository) {
    suspend operator fun invoke() = repository.processDueRules()
}
