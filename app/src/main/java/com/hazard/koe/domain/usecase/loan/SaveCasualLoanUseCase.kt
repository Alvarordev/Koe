package com.hazard.koe.domain.usecase.loan

import com.hazard.koe.domain.repository.CasualLoanRepository

class SaveCasualLoanUseCase(private val repository: CasualLoanRepository) {
    suspend operator fun invoke(
        personId: Long,
        direction: com.hazard.koe.data.enums.LoanDirection,
        amount: Long,
        currencyCode: String,
        description: String? = null,
        dueDate: Long? = null
    ): Long {
        val loan = com.hazard.koe.data.model.CasualLoan(
            personId = personId,
            direction = direction,
            originalAmount = amount,
            outstandingBalance = amount,
            currencyCode = currencyCode,
            description = description,
            dueDate = dueDate
        )
        return repository.createLoan(loan)
    }
}