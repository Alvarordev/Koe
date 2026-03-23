package com.example.tracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.yapeDataStore: DataStore<Preferences> by preferencesDataStore(name = "yape_settings")

class YapePreferences(private val context: Context) {
    private val dataStore = context.yapeDataStore

    private val YAPE_ENABLED = booleanPreferencesKey("yape_enabled")
    private val DEFAULT_ACCOUNT_ID = longPreferencesKey("default_account_id")
    private val CATEGORY_INCOME_ID = longPreferencesKey("category_income_id")
    private val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    private val CATEGORY_EXPENSE_ID = longPreferencesKey("category_expense_id")
    private val LAST_CAPTURED_DESCRIPTION = stringPreferencesKey("last_captured_description")
    private val LAST_CAPTURED_AT = longPreferencesKey("last_captured_at")

    val yapeEnabled: Flow<Boolean> = dataStore.data.map { it[YAPE_ENABLED] ?: false }
    val defaultAccountId: Flow<Long> = dataStore.data.map { it[DEFAULT_ACCOUNT_ID] ?: 0L }
    val categoryIncomeId: Flow<Long> = dataStore.data.map { it[CATEGORY_INCOME_ID] ?: 0L }
    val categoryExpenseId: Flow<Long> = dataStore.data.map { it[CATEGORY_EXPENSE_ID] ?: 0L }
    val isOnboardingComplete: Flow<Boolean> = dataStore.data.map { it[ONBOARDING_COMPLETE] ?: false }
    val lastCapturedDescription: Flow<String> = dataStore.data.map { it[LAST_CAPTURED_DESCRIPTION] ?: "" }
    val lastCapturedAt: Flow<Long> = dataStore.data.map { it[LAST_CAPTURED_AT] ?: 0L }

    suspend fun setYapeEnabled(enabled: Boolean) = dataStore.edit { it[YAPE_ENABLED] = enabled }
    suspend fun setDefaultAccountId(id: Long) = dataStore.edit { it[DEFAULT_ACCOUNT_ID] = id }
    suspend fun setCategoryIncomeId(id: Long) = dataStore.edit { it[CATEGORY_INCOME_ID] = id }
    suspend fun setCategoryExpenseId(id: Long) = dataStore.edit { it[CATEGORY_EXPENSE_ID] = id }
    suspend fun setOnboardingComplete(complete: Boolean) = dataStore.edit { it[ONBOARDING_COMPLETE] = complete }
    suspend fun setLastCaptured(description: String, at: Long) = dataStore.edit {
        it[LAST_CAPTURED_DESCRIPTION] = description
        it[LAST_CAPTURED_AT] = at
    }
    suspend fun clearConfig() = dataStore.edit { it.clear() }
}
