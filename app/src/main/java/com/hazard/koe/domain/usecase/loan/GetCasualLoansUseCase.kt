package com.hazard.koe.domain.usecase.loan

import com.hazard.koe.domain.repository.CasualLoanRepository

class GetCasualLoansUseCase(private val repository: CasualLoanRepository) {
    operator fun invoke() = repository.getActive()
}
