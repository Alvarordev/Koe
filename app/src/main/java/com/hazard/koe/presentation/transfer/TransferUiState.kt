package com.hazard.koe.presentation.transfer

import com.hazard.koe.data.model.Account

data class TransferUiState(
    val accounts: List<Account> = emptyList(),
    val sourceAccount: Account? = null,
    val destinationAccount: Account? = null,
    val amountString: String = "",
    val description: String = "",
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitSuccess: Boolean = false,
    val exchangeRate: Double = 1.0,
    val isCrossCurrency: Boolean = false
)
