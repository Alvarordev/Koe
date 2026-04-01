package com.hazard.koe.domain.usecase.loan

import com.hazard.koe.domain.repository.FormalLoanRepository

class SaveFormalLoanUseCase(private val repository: FormalLoanRepository) {
    suspend operator fun invoke(
        name: String,
        lenderName: String,
        principalAmount: Long,
        currencyCode: String,
        annualRate: Double,
        termMonths: Int,
        monthlyPayment: Long,
        accountId: Long
    ): Long {
        val monthlyRate = annualRate / 12 / 100
        val loan = com.hazard.koe.data.model.FormalLoan(
            name = name,
            lenderName = lenderName,
            principalAmount = principalAmount,
            outstandingBalance = principalAmount,
            currencyCode = currencyCode,
            annualRate = annualRate,
            monthlyRate = monthlyRate,
            termMonths = termMonths,
            monthlyPayment = monthlyPayment,
            accountId = accountId,
            startDate = System.currentTimeMillis(),
            paymentDayOfMonth = 1,
            isActive = true
        )
        return repository.create(loan)
    }
}