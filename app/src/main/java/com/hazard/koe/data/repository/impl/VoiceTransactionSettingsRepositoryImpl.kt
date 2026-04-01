package com.hazard.koe.data.repository.impl

import com.hazard.koe.data.preferences.ThemePreferences
import com.hazard.koe.domain.repository.VoiceTransactionSettingsRepository
import kotlinx.coroutines.flow.Flow

class VoiceTransactionSettingsRepositoryImpl(
    themePreferences: ThemePreferences
) : VoiceTransactionSettingsRepository {
    override val isLocationEnabled: Flow<Boolean> = themePreferences.isLocationEnabled
}
