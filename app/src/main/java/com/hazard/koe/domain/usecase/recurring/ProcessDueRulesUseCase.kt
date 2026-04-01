package com.hazard.koe.domain.usecase.recurring

import com.hazard.koe.domain.repository.RecurringRuleRepository

class ProcessDueRulesUseCase(private val repository: RecurringRuleRepository) {
    suspend operator fun invoke() = repository.processDueRules()
}
