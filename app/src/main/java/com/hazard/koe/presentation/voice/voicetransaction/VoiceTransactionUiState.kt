package com.hazard.koe.presentation.voice.voicetransaction

import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.Category

data class VoiceTransactionUiState(
    val phase: VoiceTransactionPhase = VoiceTransactionPhase.IDLE,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val recordingElapsedMillis: Long = 0L,
    val countdownSeconds: Int = RECORDING_DURATION_SECONDS,
    val rmsLevel: Float = 0f,
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedAccount: Account? = null,
    val selectedCategory: Category? = null,
    val inferredAmountMinor: Long = 0L,
    val inferredDescription: String = "",
    val inferredTransactionType: TransactionType = TransactionType.EXPENSE,
    val selectedDate: Long = System.currentTimeMillis(),
    val isLocationEnabled: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    companion object {
        const val RECORDING_DURATION_SECONDS = 10
    }
}

data class VoiceTransactionCreationResult(
    val transactionId: Long,
    val message: String,
    val undoLabel: String
)
