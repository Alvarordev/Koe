package com.example.tracker.presentation.accounts.addaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.enums.AccountType
import com.example.tracker.data.enums.CardNetwork
import com.example.tracker.data.model.Account
import com.example.tracker.domain.usecase.account.CreateAccountUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddAccountViewModel(
    private val createAccount: CreateAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAccountUiState())
    val uiState: StateFlow<AddAccountUiState> = _uiState.asStateFlow()

    fun selectType(type: AccountType) {
        val freshForm = when (type) {
            AccountType.CASH -> AddAccountFormState.CashFormState()
            AccountType.DEBIT -> AddAccountFormState.DebitFormState()
            AccountType.CREDIT -> AddAccountFormState.CreditFormState()
            AccountType.SAVINGS -> AddAccountFormState.SavingsFormState()
        }
        _uiState.update { it.copy(selectedType = type, formState = freshForm, errorMessage = null) }
    }

    fun updateName(name: String) {
        _uiState.update { state ->
            state.copy(formState = when (val form = state.formState) {
                is AddAccountFormState.CashFormState -> form.copy(name = name)
                is AddAccountFormState.DebitFormState -> form.copy(name = name)
                is AddAccountFormState.CreditFormState -> form.copy(name = name)
                is AddAccountFormState.SavingsFormState -> form.copy(name = name)
            })
        }
    }

    fun updateColor(color: String) {
        _uiState.update { state ->
            state.copy(formState = when (val form = state.formState) {
                is AddAccountFormState.CashFormState -> form.copy(color = color)
                is AddAccountFormState.DebitFormState -> form.copy(color = color)
                is AddAccountFormState.CreditFormState -> form.copy(color = color)
                is AddAccountFormState.SavingsFormState -> form.copy(color = color)
            })
        }
    }

    fun updateCurrency(currencyCode: String) {
        _uiState.update { state ->
            state.copy(formState = when (val form = state.formState) {
                is AddAccountFormState.CashFormState -> form.copy(currencyCode = currencyCode)
                is AddAccountFormState.DebitFormState -> form.copy(currencyCode = currencyCode)
                is AddAccountFormState.CreditFormState -> form.copy(currencyCode = currencyCode)
                is AddAccountFormState.SavingsFormState -> form.copy(currencyCode = currencyCode)
            })
        }
    }

    fun updateInitialBalance(value: String) {
        _uiState.update { state ->
            state.copy(formState = when (val form = state.formState) {
                is AddAccountFormState.CashFormState -> form.copy(initialBalance = value)
                is AddAccountFormState.DebitFormState -> form.copy(initialBalance = value)
                is AddAccountFormState.SavingsFormState -> form.copy(initialBalance = value)
                is AddAccountFormState.CreditFormState -> form
            })
        }
    }

    fun updateCreditLimit(value: String) {
        _uiState.update { state ->
            state.copy(formState = when (val form = state.formState) {
                is AddAccountFormState.CreditFormState -> form.copy(creditLimit = value)
                else -> form
            })
        }
    }

    fun updateCreditUsed(value: String) {
        _uiState.update { state ->
            state.copy(formState = when (val form = state.formState) {
                is AddAccountFormState.CreditFormState -> form.copy(creditUsed = value)
                else -> form
            })
        }
    }

    fun updateCardNetwork(network: CardNetwork) {
        _uiState.update { state ->
            state.copy(formState = when (val form = state.formState) {
                is AddAccountFormState.DebitFormState -> form.copy(cardNetwork = network)
                is AddAccountFormState.CreditFormState -> form.copy(cardNetwork = network)
                else -> form
            })
        }
    }

    fun updateLastFourDigits(value: String) {
        _uiState.update { state ->
            state.copy(formState = when (val form = state.formState) {
                is AddAccountFormState.DebitFormState -> form.copy(lastFourDigits = value)
                is AddAccountFormState.CreditFormState -> form.copy(lastFourDigits = value)
                else -> form
            })
        }
    }

    fun updateExpirationDate(value: String) {
        _uiState.update { state ->
            state.copy(formState = when (val form = state.formState) {
                is AddAccountFormState.DebitFormState -> form.copy(expirationDate = value)
                is AddAccountFormState.CreditFormState -> form.copy(expirationDate = value)
                else -> form
            })
        }
    }

    fun updatePaymentDay(value: String) {
        _uiState.update { state ->
            state.copy(formState = when (val form = state.formState) {
                is AddAccountFormState.CreditFormState -> form.copy(paymentDay = value)
                else -> form
            })
        }
    }

    fun updateInterestRate(value: String) {
        _uiState.update { state ->
            state.copy(formState = when (val form = state.formState) {
                is AddAccountFormState.CreditFormState -> form.copy(interestRate = value)
                is AddAccountFormState.SavingsFormState -> form.copy(interestRate = value)
                else -> form
            })
        }
    }

    fun submit() {
        val state = _uiState.value
        val form = state.formState

        if (form.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Account name is required") }
            return
        }

        val account = buildAccount(form) ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                createAccount(account)
                _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = e.message) }
            }
        }
    }

    private fun buildAccount(form: AddAccountFormState): Account? {
        return when (form) {
            is AddAccountFormState.CashFormState -> {
                val balance = (form.initialBalance.toDoubleOrNull() ?: 0.0) * 100
                Account(
                    name = form.name,
                    type = AccountType.CASH,
                    color = form.color,
                    currencyCode = form.currencyCode,
                    initialBalance = balance.toLong(),
                    currentBalance = balance.toLong()
                )
            }
            is AddAccountFormState.DebitFormState -> {
                val balance = (form.initialBalance.toDoubleOrNull() ?: 0.0) * 100
                Account(
                    name = form.name,
                    type = AccountType.DEBIT,
                    color = form.color,
                    currencyCode = form.currencyCode,
                    initialBalance = balance.toLong(),
                    currentBalance = balance.toLong(),
                    cardNetwork = form.cardNetwork,
                    lastFourDigits = form.lastFourDigits.ifBlank { null },
                    expirationDate = form.expirationDate.ifBlank { null }
                )
            }
            is AddAccountFormState.CreditFormState -> {
                val limit = (form.creditLimit.toDoubleOrNull() ?: 0.0) * 100
                val used = (form.creditUsed.toDoubleOrNull() ?: 0.0) * 100
                val rate = form.interestRate.toDoubleOrNull()?.let { it / 100.0 }
                val payDay = form.paymentDay.toIntOrNull()
                Account(
                    name = form.name,
                    type = AccountType.CREDIT,
                    color = form.color,
                    currencyCode = form.currencyCode,
                    initialBalance = 0L,
                    currentBalance = 0L,
                    cardNetwork = form.cardNetwork,
                    lastFourDigits = form.lastFourDigits.ifBlank { null },
                    expirationDate = form.expirationDate.ifBlank { null },
                    creditLimit = limit.toLong(),
                    creditUsed = used.toLong(),
                    paymentDay = payDay,
                    interestRate = rate
                )
            }
            is AddAccountFormState.SavingsFormState -> {
                val balance = (form.initialBalance.toDoubleOrNull() ?: 0.0) * 100
                val rate = form.interestRate.toDoubleOrNull()?.let { it / 100.0 }
                Account(
                    name = form.name,
                    type = AccountType.SAVINGS,
                    color = form.color,
                    currencyCode = form.currencyCode,
                    initialBalance = balance.toLong(),
                    currentBalance = balance.toLong(),
                    interestRate = rate
                )
            }
        }
    }
}
