package com.hazard.koe.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.exchangeRateDataStore: DataStore<Preferences> by preferencesDataStore(name = "exchange_rates")

class ExchangeRatePreferences(private val context: Context) {

    companion object {
        private const val DEFAULT_USD_TO_PEN = 3.35
    }

    private val usdToPenKey = doublePreferencesKey("usd_to_pen")
    private val lastUpdatedKey = longPreferencesKey("last_updated")

    val usdToPen: Flow<Double> = context.exchangeRateDataStore.data.map { prefs ->
        prefs[usdToPenKey] ?: DEFAULT_USD_TO_PEN
    }

    val lastUpdated: Flow<Long> = context.exchangeRateDataStore.data.map { prefs ->
        prefs[lastUpdatedKey] ?: 0L
    }

    suspend fun setUsdToPen(rate: Double) {
        context.exchangeRateDataStore.edit { prefs ->
            prefs[usdToPenKey] = rate
            prefs[lastUpdatedKey] = System.currentTimeMillis()
        }
    }

    suspend fun getRate(fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency == toCurrency) return 1.0
        val usdPen = usdToPen.first()
        return when {
            fromCurrency == "USD" && toCurrency == "PEN" -> usdPen
            fromCurrency == "PEN" && toCurrency == "USD" -> 1.0 / usdPen
            else -> 1.0
        }
    }
}
