package com.example.tracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tracker_settings")

class ThemePreferences(private val context: Context) {

    private val isDarkModeKey = booleanPreferencesKey("is_dark_mode")
    private val LOCATION_ENABLED_KEY = booleanPreferencesKey("location_enabled")

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[isDarkModeKey] ?: false
    }

    val isLocationEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[LOCATION_ENABLED_KEY] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[isDarkModeKey] = enabled
        }
    }

    suspend fun setLocationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[LOCATION_ENABLED_KEY] = enabled }
    }
}
