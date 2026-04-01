package com.hazard.koe.presentation.loans.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazard.koe.data.enums.LoanDirection
import com.hazard.koe.domain.repository.CasualLoanRepository
import com.hazard.koe.domain.repository.PersonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CasualLoanDetailViewModel(
    private val personId: Long,
    private val personRepository: PersonRepository,
    private val casualLoanRepository: CasualLoanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CasualLoanDetailUiState())
    val uiState: StateFlow<CasualLoanDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                personRepository.getById(personId),
                casualLoanRepository.getByPerson(personId)
            ) { person, loans ->
                val loansWithTransactions = loans.map { loan ->
                    val transactions = casualLoanRepository.getTransactions(loan.id).first()
                    CasualLoanWithTransactions(loan, transactions)
                }

                val totalLend = loans.filter { it.direction == LoanDirection.LENT }
                    .sumOf { it.outstandingBalance }
                val totalBorrow = loans.filter { it.direction == LoanDirection.BORROWED }
                    .sumOf { it.outstandingBalance }

                CasualLoanDetailUiState(
                    person = person,
                    loans = loansWithTransactions,
                    totalLend = totalLend,
                    totalBorrow = totalBorrow,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun recordPayment(loanId: Long, amount: Long, accountId: Long, note: String?) {
        viewModelScope.launch {
            try {
                casualLoanRepository.recordPayment(loanId, amount, accountId, note)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun markAsPaidOff(loanId: Long) {
        viewModelScope.launch {
            try {
                casualLoanRepository.markPaidOff(loanId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}