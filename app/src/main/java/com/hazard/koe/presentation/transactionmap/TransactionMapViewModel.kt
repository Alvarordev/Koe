package com.hazard.koe.presentation.transactionmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazard.koe.domain.usecase.transaction.GetTransactionsWithCoordinatesByMonthUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionMapViewModel(
    private val getTransactionsWithCoordinates: GetTransactionsWithCoordinatesByMonthUseCase
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    private val _selectedCluster = MutableStateFlow<MapCluster?>(null)

    private val clustersFlow = _selectedMonth
        .flatMapLatest { month ->
            getTransactionsWithCoordinates(month).map { transactions ->
                val pins = transactions.map { txn ->
                    TransactionMapPin(
                        id = txn.id,
                        lat = txn.latitude,
                        lng = txn.longitude,
                        emoji = txn.categoryEmoji,
                        colorHex = normalizeColorHex(txn.categoryColor),
                        amountFormatted = String.format("%.2f", txn.amount / 100.0)
                    )
                }
                Pair(month, clusterPins(pins))
            }
        }

    val uiState = combine(clustersFlow, _selectedCluster) { (month, clusters), selectedCluster ->
        TransactionMapUiState(
            selectedMonth = month,
            clusters = clusters,
            selectedCluster = selectedCluster,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionMapUiState()
    )

    fun previousMonth() {
        _selectedCluster.value = null
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedCluster.value = null
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun selectCluster(cluster: MapCluster) {
        _selectedCluster.value = cluster
    }

    fun dismissCluster() {
        _selectedCluster.value = null
    }

    private fun normalizeColorHex(color: String): String {
        return if (color.startsWith("#")) color else "#$color"
    }

    private fun haversineDistanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun clusterPins(pins: List<TransactionMapPin>): List<MapCluster> {
        val assigned = BooleanArray(pins.size) { false }
        val clusters = mutableListOf<MapCluster>()

        for (i in pins.indices) {
            if (assigned[i]) continue
            val group = mutableListOf(pins[i])
            assigned[i] = true
            for (j in (i + 1) until pins.size) {
                if (assigned[j]) continue
                val dist = haversineDistanceMeters(
                    pins[i].lat, pins[i].lng,
                    pins[j].lat, pins[j].lng
                )
                if (dist <= 500.0) {
                    group.add(pins[j])
                    assigned[j] = true
                }
            }
            val centroidLat = group.map { it.lat }.average()
            val centroidLng = group.map { it.lng }.average()
            val stableId = "cluster_${group.sortedBy { it.id }.first().id}"
            clusters.add(MapCluster(id = stableId, lat = centroidLat, lng = centroidLng, pins = group))
        }

        return clusters
    }
}
