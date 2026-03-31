package com.example.tracker.domain.usecase.loan

import com.example.tracker.domain.repository.CasualLoanRepository

class SaveCasualLoanUseCase(private val repository: CasualLoanRepository) {
    suspend operator fun invoke(
        personId: Long,
        direction: com.example.tracker.data.enums.LoanDirection,
        amount: Long,
        currencyCode: String,
        description: String? = null,
        dueDate: Long? = null
    ): Long {
        val loan = com.example.tracker.data.model.CasualLoan(
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