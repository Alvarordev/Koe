package com.example.tracker.domain.usecase.budget

import com.example.tracker.data.model.Budget
import com.example.tracker.domain.repository.BudgetRepository

class CreateBudgetUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(budget: Budget) = repository.create(budget)
}
