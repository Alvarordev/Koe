package com.example.tracker.presentation.settings.yapesetup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.enums.CategoryType
import com.example.tracker.data.model.Account
import com.example.tracker.data.model.Category
import com.example.tracker.data.preferences.YapePreferences
import com.example.tracker.domain.usecase.account.GetAccountsUseCase
import com.example.tracker.domain.usecase.category.CreateCategoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class YapeSetupViewModel(
    private val getAccounts: GetAccountsUseCase,
    private val createCategory: CreateCategoryUseCase,
    private val yapePreferences: YapePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(YapeSetupUiState())
    val uiState: StateFlow<YapeSetupUiState> = _uiState.asStateFlow()

    init {
        getAccounts()
            .onEach { accounts ->
                _uiState.update { it.copy(accounts = accounts.filter { a -> !a.isArchived }) }
            }
            .launchIn(viewModelScope)

        combine(
            yapePreferences.yapeEnabled,
            yapePreferences.lastCapturedDescription,
            yapePreferences.lastCapturedAt
        ) { enabled, desc, at ->
            Triple(enabled, desc, at)
        }.onEach { (enabled, desc, at) ->
            _uiState.update { it.copy(yapeEnabled = enabled, lastCapturedDescription = desc, lastCapturedAt = at) }
        }.launchIn(viewModelScope)
    }

    fun onAccountSelected(account: Account) {
        _uiState.update { it.copy(selectedAccount = account) }
    }

    fun onNextFromIntro() {
        _uiState.update { it.copy(currentStep = YapeSetupStep.ACCOUNT) }
    }

    fun onNextFromAccount() {
        _uiState.update { it.copy(currentStep = YapeSetupStep.PERMISSION) }
    }

    fun onPermissionGranted() {
        val selectedAccount = _uiState.value.selectedAccount ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val onboardingAlreadyDone = yapePreferences.isOnboardingComplete.first()
            if (!onboardingAlreadyDone) {
                val incomeId = createCategory(
                    Category(
                        name = "Yape Recibido",
                        emoji = "\uD83D\uDCF1",
                        color = "#4CAF50",
                        type = CategoryType.INCOME,
                        isSystem = false
                    )
                )
                yapePreferences.setCategoryIncomeId(incomeId)

                val expenseId = createCategory(
                    Category(
                        name = "Yape",
                        emoji = "\uD83D\uDCF1",
                        color = "#7C4DFF",
                        type = CategoryType.EXPENSE,
                        isSystem = false
                    )
                )
                yapePreferences.setCategoryExpenseId(expenseId)
            }
            yapePreferences.setDefaultAccountId(selectedAccount.id)
            yapePreferences.setYapeEnabled(true)
            yapePreferences.setOnboardingComplete(true)
            _uiState.update { it.copy(isLoading = false, currentStep = YapeSetupStep.DONE) }
        }
    }

    fun onDeactivate() {
        viewModelScope.launch {
            yapePreferences.clearConfig()
            _uiState.update { it.copy(yapeEnabled = false, currentStep = YapeSetupStep.INTRO) }
        }
    }
}
