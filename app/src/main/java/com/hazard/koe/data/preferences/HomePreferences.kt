package com.hazard.koe.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hazard.koe.domain.model.HomeDateFilterPreset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.homeDataStore: DataStore<Preferences> by preferencesDataStore(name = "home_settings")

class HomePreferences(private val context: Context) {

    private val dateFilterPresetKey = stringPreferencesKey("date_filter_preset")

    val dateFilterPreset: Flow<HomeDateFilterPreset> = context.homeDataStore.data.map { preferences ->
        val savedValue = preferences[dateFilterPresetKey]
        savedValue.toHomeDateFilterPresetOrDefault()
    }

    suspend fun setDateFilterPreset(preset: HomeDateFilterPreset) {
        context.homeDataStore.edit { preferences ->
            preferences[dateFilterPresetKey] = preset.name
        }
    }

    private fun String?.toHomeDateFilterPresetOrDefault(): HomeDateFilterPreset {
        if (this == null) return HomeDateFilterPreset.MONTH
        return try {
            HomeDateFilterPreset.valueOf(this)
        } catch (_: IllegalArgumentException) {
            HomeDateFilterPreset.MONTH
        }
    }
}
