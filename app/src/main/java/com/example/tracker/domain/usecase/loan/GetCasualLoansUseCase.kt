package com.example.tracker.domain.usecase.loan

import com.example.tracker.domain.repository.CasualLoanRepository

class GetCasualLoansUseCase(private val repository: CasualLoanRepository) {
    operator fun invoke() = repository.getActive()
}
