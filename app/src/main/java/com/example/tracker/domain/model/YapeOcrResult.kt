package com.example.tracker.domain.model

data class YapeOcrResult(
    val amountCents: Long,
    val recipientName: String?,
    val dateMillis: Long?,
    val operationNumber: String?
)
