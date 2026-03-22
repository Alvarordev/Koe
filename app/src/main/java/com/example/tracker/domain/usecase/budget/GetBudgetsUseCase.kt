package com.example.tracker.domain.usecase.budget

import com.example.tracker.domain.repository.BudgetRepository

class GetBudgetsUseCase(private val repository: BudgetRepository) {
    operator fun invoke() = repository.getAll()
}
