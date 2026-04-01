package com.hazard.koe.domain.model

import com.hazard.koe.data.enums.TransactionType

data class VoiceTransactionInferenceResult(
    val amountMinor: Long,
    val transactionType: TransactionType,
    val categoryId: Long?,
    val accountId: Long?,
    val description: String?,
    val confidence: Float,
    val reasoning: String?
)
