package com.hazard.koe.domain.usecase.home

import com.hazard.koe.domain.model.HomeDateFilterPreset
import com.hazard.koe.domain.repository.HomePreferencesRepository

class SaveHomeDateFilterPresetUseCase(
    private val repository: HomePreferencesRepository
) {
    suspend operator fun invoke(preset: HomeDateFilterPreset) {
        repository.setDateFilterPreset(preset)
    }
}
