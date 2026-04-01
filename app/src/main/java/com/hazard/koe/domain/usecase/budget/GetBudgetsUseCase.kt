package com.hazard.koe.domain.usecase.budget

import com.hazard.koe.domain.repository.BudgetRepository

class GetBudgetsUseCase(private val repository: BudgetRepository) {
    operator fun invoke() = repository.getAll()
}
