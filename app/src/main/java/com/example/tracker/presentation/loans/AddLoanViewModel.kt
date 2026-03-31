package com.example.tracker.presentation.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.enums.LoanDirection
import com.example.tracker.data.model.Person
import com.example.tracker.domain.usecase.account.GetAccountsUseCase
import com.example.tracker.domain.usecase.loan.SaveCasualLoanUseCase
import com.example.tracker.domain.usecase.loan.SaveFormalLoanUseCase
import com.example.tracker.domain.usecase.person.GetPersonsUseCase
import com.example.tracker.domain.usecase.person.SavePersonUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddLoanViewModel(
    private val getPersons: GetPersonsUseCase,
    private val savePersonUseCase: SavePersonUseCase,
    private val saveCasualLoanUseCase: SaveCasualLoanUseCase,
    private val saveFormalLoanUseCase: SaveFormalLoanUseCase,
    private val getAccounts: GetAccountsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddLoanUiState())
    val uiState: StateFlow<AddLoanUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            val accounts = getAccounts().first().filter { !it.isArchived }
            _uiState.update { it.copy(accounts = accounts, selectedAccountId = accounts.firstOrNull()?.id) }
        }
    }

    fun onPersonNameChange(name: String) {
        _uiState.update { it.copy(personName = name) }
        if (name.length >= 2) {
            searchPersons(name)
        } else {
            _uiState.update { it.copy(showPersonSuggestions = false, suggestedPersons = emptyList()) }
        }
    }

    private fun searchPersons(query: String) {
        viewModelScope.launch {
            val persons = getPersons().first()
            val suggestions = persons.filter { 
                it.name.contains(query, ignoreCase = true) 
            }.take(5).map { PersonSuggestion(it.id, it.name, it.emoji) }
            
            _uiState.update { 
                it.copy(
                    showPersonSuggestions = suggestions.isNotEmpty(),
                    suggestedPersons = suggestions
                ) 
            }
        }
    }

    fun onPersonSelected(person: PersonSuggestion) {
        _uiState.update { 
            it.copy(
                personName = person.name,
                selectedPersonId = person.id,
                showPersonSuggestions = false,
                suggestedPersons = emptyList()
            ) 
        }
    }

    fun onLoanTypeChange(type: LoanType) {
        _uiState.update { it.copy(loanType = type) }
    }

    fun onDirectionChange(direction: LoanDirection) {
        _uiState.update { it.copy(direction = direction) }
    }

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onCurrencyChange(currency: String) {
        _uiState.update { it.copy(currencyCode = currency) }
    }

    fun onDescriptionChange(desc: String) {
        _uiState.update { it.copy(description = desc) }
    }

    fun onFormalLoanNameChange(name: String) {
        _uiState.update { it.copy(formalLoanName = name) }
    }

    fun onLenderNameChange(name: String) {
        _uiState.update { it.copy(lenderName = name) }
    }

    fun onAnnualRateChange(rate: String) {
        _uiState.update { it.copy(annualRate = rate) }
    }

    fun onTermMonthsChange(months: String) {
        _uiState.update { it.copy(termMonths = months) }
    }

    fun onMonthlyPaymentChange(payment: String) {
        _uiState.update { it.copy(monthlyPayment = payment) }
    }

    fun onAccountSelected(accountId: Long) {
        _uiState.update { it.copy(selectedAccountId = accountId) }
    }

    fun saveLoan() {
        val state = _uiState.value
        
        if (state.loanType == LoanType.CASUAL) {
            saveCasualLoan(state)
        } else {
            saveFormalLoan(state)
        }
    }

    private fun saveFormalLoan(state: AddLoanUiState) {
        viewModelScope.launch {
            try {
                val amount = state.amount.toLongOrNull()
                if (amount == null || amount <= 0) {
                    _uiState.update { it.copy(error = "Amount must be greater than 0") }
                    return@launch
                }

                val annualRate = state.annualRate.toDoubleOrNull() ?: 0.0
                val termMonths = state.termMonths.toIntOrNull() ?: 0
                val monthlyPayment = state.monthlyPayment.toLongOrNull() ?: 0L

                if (state.formalLoanName.isBlank() || state.lenderName.isBlank()) {
                    _uiState.update { it.copy(error = "Please fill all required fields") }
                    return@launch
                }

                val accountId = state.selectedAccountId
                if (accountId == null) {
                    _uiState.update { it.copy(error = "Please select an account") }
                    return@launch
                }

                _uiState.update { it.copy(isSaving = true) }

                saveFormalLoanUseCase(
                    name = state.formalLoanName,
                    lenderName = state.lenderName,
                    principalAmount = amount,
                    currencyCode = state.currencyCode,
                    annualRate = annualRate,
                    termMonths = termMonths,
                    monthlyPayment = monthlyPayment,
                    accountId = accountId
                )

                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Error saving loan") }
            }
        }
    }

    private fun saveCasualLoan(state: AddLoanUiState) {
        viewModelScope.launch {
            try {
                val amount = state.amount.toLongOrNull()
                if (amount == null || amount <= 0) {
                    _uiState.update { it.copy(error = "Amount must be greater than 0") }
                    return@launch
                }

                val personId = state.selectedPersonId ?: createOrGetPerson(state.personName)

                if (personId == null) {
                    _uiState.update { it.copy(error = "Error creating person") }
                    return@launch
                }

                _uiState.update { it.copy(isSaving = true) }

                saveCasualLoanUseCase(
                    personId = personId,
                    direction = state.direction,
                    amount = amount,
                    currencyCode = state.currencyCode,
                    description = state.description.ifBlank { null }
                )

                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Error saving loan") }
            }
        }
    }

    private suspend fun createOrGetPerson(name: String): Long? {
        val persons = getPersons().first()
        
        val existingPerson = persons.find { it.name.equals(name, ignoreCase = true) }
        if (existingPerson != null) {
            return existingPerson.id
        }

        var newName = name
        var counter = 2
        while (persons.any { it.name.equals(newName, ignoreCase = true) }) {
            newName = "$name ($counter)"
            counter++
        }

        return savePersonUseCase(newName)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}