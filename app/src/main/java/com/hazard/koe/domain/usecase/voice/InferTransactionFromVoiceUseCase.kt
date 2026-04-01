package com.hazard.koe.domain.usecase.voice

import com.hazard.koe.domain.model.VoiceTransactionInferenceRequest
import com.hazard.koe.domain.repository.VoiceTransactionInferenceRepository

class InferTransactionFromVoiceUseCase(
    private val repository: VoiceTransactionInferenceRepository
) {
    suspend operator fun invoke(request: VoiceTransactionInferenceRequest) = repository.infer(request)
}
