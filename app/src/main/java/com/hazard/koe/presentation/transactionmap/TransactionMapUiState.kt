package com.hazard.koe.presentation.transactionmap

import java.time.YearMonth

data class TransactionMapPin(
    val id: Long,
    val lat: Double,
    val lng: Double,
    val emoji: String,
    val colorHex: String,
    val amountFormatted: String
)

data class MapCluster(
    val id: String,
    val lat: Double,
    val lng: Double,
    val pins: List<TransactionMapPin>
)

data class TransactionMapUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val clusters: List<MapCluster> = emptyList(),
    val selectedCluster: MapCluster? = null,
    val isLoading: Boolean = true
)
