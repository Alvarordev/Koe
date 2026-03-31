package com.example.tracker.presentation.loans.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.domain.repository.FormalLoanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FormalLoanDetailViewModel(
    private val loanId: Long,
    private val formalLoanRepository: FormalLoanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormalLoanDetailUiState())
    val uiState: StateFlow<FormalLoanDetailUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                formalLoanRepository.getById(loanId),
                formalLoanRepository.getPayments(loanId)
            ) { loan, payments ->
                FormalLoanDetailUiState(
                    loan = loan,
                    payments = payments,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun recordPayment(accountId: Long) {
        viewModelScope.launch {
            try {
                formalLoanRepository.recordPayment(loanId, accountId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}