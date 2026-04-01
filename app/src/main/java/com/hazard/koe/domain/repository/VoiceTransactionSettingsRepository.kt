package com.hazard.koe.domain.repository

import kotlinx.coroutines.flow.Flow

interface VoiceTransactionSettingsRepository {
    val isLocationEnabled: Flow<Boolean>
}
