package com.hazard.koe.domain.model

import com.hazard.koe.data.enums.CategoryType

data class VoiceTransactionInferenceRequest(
    val audioBytes: ByteArray = byteArrayOf(),
    val audioMimeType: String = "audio/mp4",
    val transcript: String? = null,
    val localeTag: String,
    val accounts: List<VoiceAccountContext>,
    val categories: List<VoiceCategoryContext>
)

data class VoiceAccountContext(
    val id: Long,
    val name: String,
    val currencyCode: String
)

data class VoiceCategoryContext(
    val id: Long,
    val name: String,
    val type: CategoryType
)
