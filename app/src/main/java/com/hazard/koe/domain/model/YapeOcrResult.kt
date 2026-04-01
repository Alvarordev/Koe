package com.hazard.koe.domain.model

data class YapeOcrResult(
    val amountCents: Long,
    val recipientName: String?,
    val dateMillis: Long?,
    val operationNumber: String?
)
