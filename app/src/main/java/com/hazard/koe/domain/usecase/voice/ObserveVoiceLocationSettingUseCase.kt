package com.hazard.koe.domain.usecase.voice

import com.hazard.koe.domain.repository.VoiceTransactionSettingsRepository

class ObserveVoiceLocationSettingUseCase(
    private val repository: VoiceTransactionSettingsRepository
) {
    operator fun invoke() = repository.isLocationEnabled
}
