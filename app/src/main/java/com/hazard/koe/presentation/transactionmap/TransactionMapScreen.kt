package com.hazard.koe.presentation.transactionmap

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.vanniktech.emoji.EmojiTextView
import org.koin.androidx.compose.koinViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val limaLatLng = LatLng(-12.0464, -77.0428)

private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "PE"))

private fun createSinglePinMarker(context: Context, emoji: String, colorHex: String): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val size = (52 * density).toInt()
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val fillPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    fillPaint.color = colorHex.toColorInt()
    canvas.drawCircle(size / 2f, size / 2f, size / 2f * 0.9f, fillPaint)

    val borderPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    borderPaint.color = android.graphics.Color.WHITE
    borderPaint.style = android.graphics.Paint.Style.STROKE
    borderPaint.strokeWidth = 3 * density
    canvas.drawCircle(size / 2f, size / 2f, size / 2f * 0.9f, borderPaint)

    val emojiView = EmojiTextView(context).apply {
        text = emoji
        textSize = 22f
        measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        layout(0, 0, measuredWidth, measuredHeight)
    }
    canvas.save()
    canvas.translate(size / 2f - emojiView.measuredWidth / 2f, size / 2f - emojiView.measuredHeight / 2f)
    emojiView.draw(canvas)
    canvas.restore()

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

private fun createClusterMarker(context: Context, cluster: MapCluster): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val circleDp = 44
    val overlapDp = 12
    val circleSize = (circleDp * density).toInt()
    val overlap = (overlapDp * density).toInt()
    val visiblePins = cluster.pins.take(3)
    val bitmapWidth = circleSize + (visiblePins.size - 1) * (circleSize - overlap)
    val bitmapHeight = (52 * density).toInt()
    val verticalOffset = (bitmapHeight - circleSize) / 2

    val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val fillPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    val borderPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 3 * density
    }

    for (idx in visiblePins.indices.reversed()) {
        val pin = visiblePins[idx]
        val cx = idx * (circleSize - overlap) + circleSize / 2f
        val cy = verticalOffset + circleSize / 2f
        val radius = circleSize / 2f * 0.9f

        fillPaint.color = pin.colorHex.toColorInt()
        canvas.drawCircle(cx, cy, radius, fillPaint)
        canvas.drawCircle(cx, cy, radius, borderPaint)

        val emojiView = EmojiTextView(context).apply {
            text = pin.emoji
            textSize = 18f
            measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            layout(0, 0, measuredWidth, measuredHeight)
        }
        canvas.save()
        canvas.translate(cx - emojiView.measuredWidth / 2f, cy - emojiView.measuredHeight / 2f)
        emojiView.draw(canvas)
        canvas.restore()
    }

    if (cluster.pins.size > 3) {
        val badgeRadius = (10 * density)
        val badgeCx = bitmapWidth - badgeRadius
        val badgeCy = verticalOffset.toFloat()

        val badgeFill = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
        }
        canvas.drawCircle(badgeCx, badgeCy, badgeRadius, badgeFill)

        val badgeBorder = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.DKGRAY
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = density
        }
        canvas.drawCircle(badgeCx, badgeCy, badgeRadius, badgeBorder)

        val badgeText = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.DKGRAY
            textSize = 8 * density
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val extraCount = cluster.pins.size - 3
        val label = "+$extraCount"
        val textY = badgeCy - (badgeText.descent() + badgeText.ascent()) / 2
        canvas.drawText(label, badgeCx, textY, badgeText)
    }

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionMapScreen(
    onBack: () -> Unit,
    viewModel: TransactionMapViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(limaLatLng, 5f)
    }

    LaunchedEffect(uiState.clusters) {
        val clusters = uiState.clusters
        when {
            clusters.isEmpty() -> {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(limaLatLng, 5f)
                )
            }
            clusters.size == 1 && clusters.first().pins.size == 1 -> {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(clusters.first().lat, clusters.first().lng),
                        15f
                    )
                )
            }
            else -> {
                val builder = LatLngBounds.Builder()
                clusters.forEach { cluster -> builder.include(LatLng(cluster.lat, cluster.lng)) }
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(builder.build(), 160)
                )
            }
        }
    }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mapa de gastos",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MonthSelectorRow(
                selectedMonth = uiState.selectedMonth,
                onPreviousMonth = viewModel::previousMonth,
                onNextMonth = viewModel::nextMonth
            )

            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        compassEnabled = false,
                        mapToolbarEnabled = false,
                        scrollGesturesEnabled = true,
                        zoomGesturesEnabled = true
                    ),
                    googleMapOptionsFactory = {
                        com.google.android.gms.maps.GoogleMapOptions()
                            .mapColorScheme(
                                if (isDark) MapColorScheme.DARK else MapColorScheme.LIGHT
                            )
                    }
                ) {
                    uiState.clusters.forEach { cluster ->
                        val position = LatLng(cluster.lat, cluster.lng)
                        val markerState = rememberUpdatedMarkerState(position = position)
                        val icon = remember(cluster.id) {
                            runCatching {
                                if (cluster.pins.size == 1) {
                                    createSinglePinMarker(context, cluster.pins.first().emoji, cluster.pins.first().colorHex)
                                } else {
                                    createClusterMarker(context, cluster)
                                }
                            }.getOrNull()
                        }
                        Marker(
                            state = markerState,
                            icon = icon,
                            onClick = {
                                viewModel.selectCluster(cluster)
                                true
                            }
                        )
                    }
                }
            }
        }

        if (uiState.selectedCluster != null) {
            ModalBottomSheet(
                onDismissRequest = viewModel::dismissCluster,
                sheetState = bottomSheetState
            ) {
                ClusterBottomSheet(cluster = uiState.selectedCluster!!)
            }
        }
    }
}

@Composable
private fun ClusterBottomSheet(cluster: MapCluster) {
    val title = if (cluster.pins.size == 1) {
        "1 transacción"
    } else {
        "${cluster.pins.size} transacciones"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cluster.pins) { pin ->
                PinItem(pin = pin)
            }
        }
    }
}

@Composable
private fun PinItem(pin: TransactionMapPin) {
    val circleColor = remember(pin.colorHex) {
        runCatching { Color(pin.colorHex.toColorInt()) }.getOrElse { Color.Gray }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(circleColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = pin.emoji,
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "S/${pin.amountFormatted}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MonthSelectorRow(
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier.size(40.dp)
        ) {
            Text(
                text = "◄",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = selectedMonth.format(monthFormatter)
                .replaceFirstChar { it.uppercaseChar() },
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(
            onClick = onNextMonth,
            modifier = Modifier.size(40.dp)
        ) {
            Text(
                text = "►",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
