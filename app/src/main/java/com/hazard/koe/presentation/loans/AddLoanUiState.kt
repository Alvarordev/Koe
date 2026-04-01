package com.hazard.koe.presentation.loans

import com.hazard.koe.data.enums.LoanDirection

data class AddLoanUiState(
    val loanType: LoanType = LoanType.CASUAL,
    val direction: LoanDirection = LoanDirection.LENT,
    val personName: String = "",
    val suggestedPersons: List<PersonSuggestion> = emptyList(),
    val showPersonSuggestions: Boolean = false,
    val selectedPersonId: Long? = null,
    val amount: String = "",
    val currencyCode: String = "USD",
    val description: String = "",
    val formalLoanName: String = "",
    val lenderName: String = "",
    val annualRate: String = "",
    val termMonths: String = "",
    val monthlyPayment: String = "",
    val accounts: List<com.hazard.koe.data.model.Account> = emptyList(),
    val selectedAccountId: Long? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

data class PersonSuggestion(
    val id: Long,
    val name: String,
    val emoji: String?
)

enum class LoanType {
    CASUAL,
    FORMAL
}