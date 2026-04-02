package com.hazard.koe.domain.repository

import com.hazard.koe.domain.model.HomeDateFilterPreset
import kotlinx.coroutines.flow.Flow

interface HomePreferencesRepository {
    val dateFilterPreset: Flow<HomeDateFilterPreset>
    suspend fun setDateFilterPreset(preset: HomeDateFilterPreset)
}
