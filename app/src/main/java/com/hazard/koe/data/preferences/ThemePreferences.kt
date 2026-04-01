package com.hazard.koe.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tracker_settings")

enum class ThemePreference {
    System, Light, Dark
}

class ThemePreferences(private val context: Context) {

    private val themeKey = stringPreferencesKey("theme_preference")
    private val LOCATION_ENABLED_KEY = booleanPreferencesKey("location_enabled")

    val themePreference: Flow<ThemePreference> = context.dataStore.data.map { preferences ->
        val themeName = preferences[themeKey] ?: ThemePreference.System.name
        try {
            ThemePreference.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            ThemePreference.System
        }
    }

    val isDarkMode: Flow<Boolean> = themePreference.map { theme ->
        when (theme) {
            ThemePreference.Light -> false
            ThemePreference.Dark -> true
            ThemePreference.System -> false // Will be resolved at runtime
        }
    }

    val isLocationEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[LOCATION_ENABLED_KEY] ?: false
    }

    suspend fun setThemePreference(preference: ThemePreference) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = preference.name
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = if (enabled) ThemePreference.Dark.name else ThemePreference.Light.name
        }
    }

    suspend fun setLocationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[LOCATION_ENABLED_KEY] = enabled }
    }
}