package com.hazard.koe.presentation.voice.voicetransaction

import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.Category

data class VoiceTransactionUiState(
    val phase: VoiceTransactionPhase = VoiceTransactionPhase.IDLE,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val confidence: Float = 0f,
    val confidenceThreshold: Float = 0.75f,
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedAccount: Account? = null,
    val selectedCategory: Category? = null,
    val amountMinor: Long = 0L,
    val description: String = "",
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val selectedDate: Long = System.currentTimeMillis(),
    val isLocationEnabled: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val saveSuccess: Boolean = false
)
