package com.example.tracker.domain.usecase.loan

import com.example.tracker.domain.repository.FormalLoanRepository

class GetFormalLoansUseCase(private val repository: FormalLoanRepository) {
    operator fun invoke() = repository.getActive()
}
