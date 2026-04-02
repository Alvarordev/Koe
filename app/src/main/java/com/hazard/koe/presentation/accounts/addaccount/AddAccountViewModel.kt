package com.hazard.koe.presentation.accounts.addaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.enums.CardNetwork
import com.hazard.koe.data.model.Account
import com.hazard.koe.domain.exception.CreditLimitExceededException
import com.hazard.koe.domain.usecase.account.CreateAccountUseCase
import com.hazard.koe.domain.usecase.account.GetAccountByIdUseCase
import com.hazard.koe.domain.usecase.account.UpdateAccountUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddAccountViewModel(
    private val createAccount: CreateAccountUseCase,
    private val updateAccount: UpdateAccountUseCase,
    private val getAccountById: GetAccountByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAccountUiState())
    val uiState: StateFlow<AddAccountUiState> = _uiState.asStateFlow()

    private var editingAccountId: Long? = null
    private var originalAccount: Account? = null

    fun loadAccountForEdit(accountId: Long) {
        viewModelScope.launch {
            val account = getAccountById(accountId).firstOrNull() ?: return@launch
            editingAccountId = account.id
            originalAccount = account

            val formState = when (account.type) {
                AccountType.CASH -> AddAccountFormState.CashFormState(
                    name = account.name,
                    color = account.color,
                    currencyCode = account.currencyCode,
                    initialBalance = formatMinorToDisplay(account.initialBalance)
                )
                AccountType.DEBIT -> AddAccountFormState.DebitFormState(
                    name = account.name,
                    color = account.color,
                    currencyCode = account.currencyCode,
                    initialBalance = formatMinorToDisplay(account.initialBalance),
                    cardNetwork = account.cardNetwork ?: CardNetwork.VISA,
                    lastFourDigits = account.lastFourDigits ?: "",
                    expirationDate = account.expirationDate ?: ""
                )
                AccountType.CREDIT -> AddAccountFormState.CreditFormState(
                    name = account.name,
                    color = account.color,
                    currencyCode = account.currencyCode,
                    creditLimit = formatMinorToDisplay(account.creditLimit ?: 0L),
                    creditUsed = formatMinorToDisplay(account.creditUsed ?: 0L),
                    cardNetwork = account.cardNetwork ?: CardNetwork.VISA,
                    lastFourDigits = account.lastFourDigits ?: "",
                    expirationDate = account.expirationDate ?: "",
                    paymentDay = account.paymentDay?.toString() ?: "",
                    closingDay = account.closingDay?.toString() ?: "",
                    interestRate = account.interestRate?.let { "%.2f".format(it * 100) } ?: ""
                )
                AccountType.SAVINGS -> AddAccountFormState.SavingsFormState(
                    name = account.name,
                    color = account.color,
                    currencyCode = account.currencyCode,
                    initialBalance = formatMinorToDisplay(account.initialBalance),
                    interestRate = account.interestRate?.let { "%.2f".format(it * 100) } ?: ""
                )
            }

            _uiState.update {
                it.copy(
                    selectedType = account.type,
                    formState = formState,
                    isEditing = true
                )
            }
        }
    }

    private fun formatMinorToDisplay(amountMinor: Long): String {
        if (amountMinor == 0L) return ""
        val major = amountMinor / 100.0
        return if (major == major.toLong().toDouble()) {
            major.toLong().toString()
        } else {
            "%.2f".format(major)
        }
    }

    fun selectType(type: AccountType) {
        val currentForm = _uiState.value.formState
        val freshForm = when (type) {
            AccountType.CASH -> AddAccountFormState.CashFormState(
                currencyCode = currentForm.currencyCode,
                color = currentForm.color,
                name = currentForm.name
            )
            AccountType.DEBIT -> AddAccountFormState.DebitFormState(
                currencyCode = currentForm.currencyCode,
                color = currentForm.color,
                name = currentForm.name
            )
            AccountType.CREDIT -> AddAccountFormState.CreditFormState(
                currencyCode = currentForm.currencyCode,
                color = currentForm.color,
                name = currentForm.name
            )
            AccountType.SAVINGS -> AddAccountFormState.SavingsFormState(
                currencyCode = currentForm.currencyCode,
                color = currentForm.color,
                name = currentForm.name
            )
        }
        _uiState.update { it.copy(selectedType = type, formState = freshForm, errorMessage = null) }
    }

    fun nextStep() {
        val state = _uiState.value
        if (!validateCurrentStep(state)) return

        val nextStep = state.currentStep + 1
        if (nextStep <= state.totalSteps) {
            _uiState.update { it.copy(currentStep = nextStep, errorMessage = null) }
        }
    }

    fun previousStep() {
        val state = _uiState.value
        val prevStep = state.currentStep - 1
        if (prevStep >= 1) {
            _uiState.update { it.copy(currentStep = prevStep, errorMessage = null) }
        }
    }

    private fun validateCurrentStep(state: AddAccountUiState): Boolean {
        val actualStep = if (!state.hasCardDetailsStep && state.currentStep >= 3) {
            state.currentStep + 1
        } else {
            state.currentStep
        }
        return when (actualStep) {
            1 -> true
            2 -> validateDetailsStep(state.formState)
            3 -> true
            4 -> {
                if (state.formState.name.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "El nombre de la cuenta es requerido") }
                    return false
                }
                true
            }
            5 -> true
            6 -> true
            else -> true
        }
    }

    private fun validateDetailsStep(form: AddAccountFormState): Boolean {
        when (form) {
            is AddAccountFormState.CreditFormState -> {
                if (form.paymentDay.isNotBlank()) {
                    val payDay = form.paymentDay.toIntOrNull()
                    if (payDay == null || payDay !in 1..31) {
                        _uiState.update { it.copy(errorMessage = "El día de pago debe estar entre 1 y 31") }
                        return false
                    }
                }
                if (form.closingDay.isNotBlank()) {
                    val closeDay = form.closingDay.toIntOrNull()
                    if (closeDay == null || closeDay !in 1..31) {
                        _uiState.update { it.copy(errorMessage = "El día de cierre debe estar entre 1 y 31") }
                        return false
                    }
                }
                val limit = (form.creditLimit.toDoubleOrNull() ?: 0.0) * 100
                val used = (form.creditUsed.toDoubleOrNull() ?: 0.0) * 100
                if (used.toLong() < 0L) {
                    _uiState.update { it.copy(errorMessage = "El crédito usado no puede ser negativo") }
                    return false
                }
                if (used.toLong() > limit.toLong()) {
                    _uiState.update { it.copy(errorMessage = CreditLimitExceededException().message) }
                    return false
                }
            }
            else -> {}
        }
        return true
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
        if (value.length > 4) return
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

    fun updateClosingDay(value: String) {
        _uiState.update { state ->
            state.copy(formState = when (val form = state.formState) {
                is AddAccountFormState.CreditFormState -> form.copy(closingDay = value)
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
                if (editingAccountId != null) {
                    updateAccount(account)
                } else {
                    createAccount(account)
                }
                _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = e.message) }
            }
        }
    }

    fun buildPreviewAccount(): Account {
        val form = _uiState.value.formState
        return buildAccount(form) ?: Account(
            name = form.name.ifBlank { "Cuenta" },
            type = _uiState.value.selectedType,
            color = form.color,
            currencyCode = form.currencyCode,
            initialBalance = 0L,
            currentBalance = 0L
        )
    }

    private fun buildAccount(form: AddAccountFormState): Account? {
        val existingAccount = originalAccount
        val id = editingAccountId ?: 0L
        val createdAt = existingAccount?.createdAt ?: System.currentTimeMillis()
        val sortOrder = existingAccount?.sortOrder ?: 0

        return when (form) {
            is AddAccountFormState.CashFormState -> {
                val balance = (form.initialBalance.toDoubleOrNull() ?: 0.0) * 100
                val currentBalance = if (existingAccount != null) {
                    existingAccount.currentBalance + (balance.toLong() - existingAccount.initialBalance)
                } else {
                    balance.toLong()
                }
                Account(
                    id = id,
                    name = form.name,
                    type = AccountType.CASH,
                    color = form.color,
                    currencyCode = form.currencyCode,
                    initialBalance = balance.toLong(),
                    currentBalance = currentBalance,
                    sortOrder = sortOrder,
                    createdAt = createdAt
                )
            }
            is AddAccountFormState.DebitFormState -> {
                val balance = (form.initialBalance.toDoubleOrNull() ?: 0.0) * 100
                val currentBalance = if (existingAccount != null) {
                    existingAccount.currentBalance + (balance.toLong() - existingAccount.initialBalance)
                } else {
                    balance.toLong()
                }
                Account(
                    id = id,
                    name = form.name,
                    type = AccountType.DEBIT,
                    color = form.color,
                    currencyCode = form.currencyCode,
                    initialBalance = balance.toLong(),
                    currentBalance = currentBalance,
                    cardNetwork = form.cardNetwork,
                    lastFourDigits = form.lastFourDigits.ifBlank { null },
                    expirationDate = form.expirationDate.ifBlank { null },
                    sortOrder = sortOrder,
                    createdAt = createdAt
                )
            }
            is AddAccountFormState.CreditFormState -> {
                val limit = (form.creditLimit.toDoubleOrNull() ?: 0.0) * 100
                val used = (form.creditUsed.toDoubleOrNull() ?: 0.0) * 100
                val rate = form.interestRate.toDoubleOrNull()?.let { it / 100.0 }
                val payDay = form.paymentDay.toIntOrNull()
                val closeDay = form.closingDay.toIntOrNull()

                if (form.paymentDay.isNotBlank() && payDay == null) {
                    _uiState.update { it.copy(errorMessage = "Payment day must be a valid number") }
                    return null
                }
                if (form.closingDay.isNotBlank() && closeDay == null) {
                    _uiState.update { it.copy(errorMessage = "Closing day must be a valid number") }
                    return null
                }

                if (payDay != null && payDay !in 1..31) {
                    _uiState.update { it.copy(errorMessage = "Payment day must be between 1 and 31") }
                    return null
                }
                if (closeDay != null && closeDay !in 1..31) {
                    _uiState.update { it.copy(errorMessage = "Closing day must be between 1 and 31") }
                    return null
                }
                if (used.toLong() < 0L) {
                    _uiState.update { it.copy(errorMessage = "Credit used cannot be negative") }
                    return null
                }
                if (used.toLong() > limit.toLong()) {
                    _uiState.update { it.copy(errorMessage = CreditLimitExceededException().message) }
                    return null
                }

                Account(
                    id = id,
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
                    closingDay = closeDay,
                    interestRate = rate,
                    sortOrder = sortOrder,
                    createdAt = createdAt
                )
            }
            is AddAccountFormState.SavingsFormState -> {
                val balance = (form.initialBalance.toDoubleOrNull() ?: 0.0) * 100
                val currentBalance = if (existingAccount != null) {
                    existingAccount.currentBalance + (balance.toLong() - existingAccount.initialBalance)
                } else {
                    balance.toLong()
                }
                val rate = form.interestRate.toDoubleOrNull()?.let { it / 100.0 }
                Account(
                    id = id,
                    name = form.name,
                    type = AccountType.SAVINGS,
                    color = form.color,
                    currencyCode = form.currencyCode,
                    initialBalance = balance.toLong(),
                    currentBalance = currentBalance,
                    interestRate = rate,
                    sortOrder = sortOrder,
                    createdAt = createdAt
                )
            }
        }
    }
}
