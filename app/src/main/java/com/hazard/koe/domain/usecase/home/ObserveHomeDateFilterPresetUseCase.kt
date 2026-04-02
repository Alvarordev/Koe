package com.hazard.koe.domain.usecase.home

import com.hazard.koe.domain.repository.HomePreferencesRepository

class ObserveHomeDateFilterPresetUseCase(
    private val repository: HomePreferencesRepository
) {
    operator fun invoke() = repository.dateFilterPreset
}
