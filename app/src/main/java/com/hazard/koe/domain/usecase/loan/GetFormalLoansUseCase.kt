package com.hazard.koe.domain.usecase.loan

import com.hazard.koe.domain.repository.FormalLoanRepository

class GetFormalLoansUseCase(private val repository: FormalLoanRepository) {
    operator fun invoke() = repository.getActive()
}
