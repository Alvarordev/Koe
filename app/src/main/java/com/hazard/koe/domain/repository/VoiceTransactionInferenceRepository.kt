package com.hazard.koe.domain.repository

import com.hazard.koe.domain.model.VoiceTransactionInferenceRequest
import com.hazard.koe.domain.model.VoiceTransactionInferenceResult

interface VoiceTransactionInferenceRepository {
    suspend fun infer(request: VoiceTransactionInferenceRequest): Result<VoiceTransactionInferenceResult>
}
