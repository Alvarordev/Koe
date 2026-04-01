package com.hazard.koe.domain.usecase.budget

import com.hazard.koe.data.model.Budget
import com.hazard.koe.domain.repository.BudgetRepository

class CreateBudgetUseCase(private val repository: BudgetRepository) {
    suspend operator fun invoke(budget: Budget) = repository.create(budget)
}
