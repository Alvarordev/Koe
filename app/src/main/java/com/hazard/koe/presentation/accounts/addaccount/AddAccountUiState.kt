package com.hazard.koe.presentation.accounts.addaccount

import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.enums.CardNetwork

sealed class AddAccountFormState {
    abstract val name: String
    abstract val color: String
    abstract val currencyCode: String

    data class CashFormState(
        override val name: String = "",
        override val color: String = "#1A73E8",
        override val currencyCode: String = "USD",
        val initialBalance: String = ""
    ) : AddAccountFormState()

    data class DebitFormState(
        override val name: String = "",
        override val color: String = "#1A73E8",
        override val currencyCode: String = "USD",
        val initialBalance: String = "",
        val cardNetwork: CardNetwork = CardNetwork.VISA,
        val lastFourDigits: String = "",
        val expirationDate: String = ""
    ) : AddAccountFormState()

    data class CreditFormState(
        override val name: String = "",
        override val color: String = "#1A73E8",
        override val currencyCode: String = "USD",
        val creditLimit: String = "",
        val creditUsed: String = "",
        val cardNetwork: CardNetwork = CardNetwork.VISA,
        val lastFourDigits: String = "",
        val expirationDate: String = "",
        val paymentDay: String = "",
        val closingDay: String = "",
        val interestRate: String = ""
    ) : AddAccountFormState()

    data class SavingsFormState(
        override val name: String = "",
        override val color: String = "#1A73E8",
        override val currencyCode: String = "USD",
        val initialBalance: String = "",
        val interestRate: String = ""
    ) : AddAccountFormState()
}

data class AddAccountUiState(
    val selectedType: AccountType = AccountType.CASH,
    val formState: AddAccountFormState = AddAccountFormState.CashFormState(),
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null,
    val isEditing: Boolean = false
)
