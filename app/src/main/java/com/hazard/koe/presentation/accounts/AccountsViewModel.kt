package com.hazard.koe.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazard.koe.domain.usecase.account.GetAccountsUseCase
import com.hazard.koe.domain.usecase.loan.GetCasualLoansUseCase
import com.hazard.koe.domain.usecase.loan.GetFormalLoansUseCase
import com.hazard.koe.domain.usecase.person.GetPersonsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AccountsViewModel(
    getAccounts: GetAccountsUseCase,
    getCasualLoans: GetCasualLoansUseCase,
    getFormalLoans: GetFormalLoansUseCase,
    getPersons: GetPersonsUseCase
) : ViewModel() {

    val uiState: StateFlow<AccountsUiState> = combine(
        getAccounts(),
        getCasualLoans(),
        getFormalLoans(),
        getPersons()
    ) { accounts, casualLoans, formalLoans, persons ->
        val casualLoanSummaries = casualLoans
            .groupBy { it.loan.personId }
            .map { (personId, loans) ->
                val person = loans.firstOrNull()?.person
                val lendTotal = loans.filter { it.loan.direction == com.hazard.koe.data.enums.LoanDirection.LENT }
                    .sumOf { it.loan.outstandingBalance }
                val borrowTotal = loans.filter { it.loan.direction == com.hazard.koe.data.enums.LoanDirection.BORROWED }
                    .sumOf { it.loan.outstandingBalance }
                CasualLoanSummaryWithPerson(
                    personId = personId,
                    personName = person?.name ?: "Desconocido",
                    personEmoji = person?.emoji,
                    totalLend = lendTotal,
                    totalBorrow = borrowTotal
                )
            }

        AccountsUiState(
            accounts = accounts.filter { !it.isArchived },
            casualLoanSummaries = casualLoanSummaries,
            formalLoans = formalLoans.filter { it.isActive },
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountsUiState()
    )
}
