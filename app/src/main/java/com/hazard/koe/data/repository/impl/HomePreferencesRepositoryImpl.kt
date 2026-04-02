package com.hazard.koe.data.repository.impl

import com.hazard.koe.data.preferences.HomePreferences
import com.hazard.koe.domain.model.HomeDateFilterPreset
import com.hazard.koe.domain.repository.HomePreferencesRepository
import kotlinx.coroutines.flow.Flow

class HomePreferencesRepositoryImpl(
    private val homePreferences: HomePreferences
) : HomePreferencesRepository {
    override val dateFilterPreset: Flow<HomeDateFilterPreset> = homePreferences.dateFilterPreset

    override suspend fun setDateFilterPreset(preset: HomeDateFilterPreset) {
        homePreferences.setDateFilterPreset(preset)
    }
}
