package com.hazard.koe.presentation.transactionmap

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.blur
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
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
import kotlin.math.abs

private val limaLatLng = LatLng(-12.0464, -77.0428)
private const val mapZoom = 11f

private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "PE"))

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
    for (idx in visiblePins.indices.reversed()) {
        val pin = visiblePins[idx]
        val cx = idx * (circleSize - overlap) + circleSize / 2f
        val cy = verticalOffset + circleSize / 2f
        val radius = circleSize / 2f * 0.9f

        fillPaint.color = pin.colorHex.toColorInt()
        canvas.drawCircle(cx, cy, radius, fillPaint)

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
        position = CameraPosition.fromLatLngZoom(limaLatLng, mapZoom)
    }

    LaunchedEffect(uiState.mapCenterLat, uiState.mapCenterLng) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(uiState.mapCenterLat, uiState.mapCenterLng),
                mapZoom
            )
        )
    }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                            createEmojiCircleMarker(
                                context = context,
                                emoji = cluster.pins.first().emoji,
                                colorHex = cluster.pins.first().colorHex
                            )
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

        MapHeaderOverlay(
            selectedMonth = uiState.selectedMonth,
            onBack = onBack,
            onPreviousMonth = viewModel::previousMonth,
            onNextMonth = viewModel::nextMonth,
            modifier = Modifier.align(Alignment.TopCenter)
        )

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
    onBack: () -> Unit,
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 52.dp)
        ) {
            MonthDragSelector(
                selectedMonth = selectedMonth,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth
            )
        }
    }
}

@Composable
private fun MapHeaderOverlay(
    selectedMonth: YearMonth,
    onBack: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .blur(20.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to MaterialTheme.colorScheme.background.copy(alpha = 1f),
                            0.42f to MaterialTheme.colorScheme.background.copy(alpha = 1f),
                            1f to MaterialTheme.colorScheme.background.copy(alpha = 0f)
                        )
                    )
                )
        )

        Column {
            MonthSelectorRow(
                onBack = onBack,
                selectedMonth = selectedMonth,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                modifier = Modifier
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun MonthDragSelector(
    selectedMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val dragThresholdPx = with(Density(LocalContext.current)) { 56.dp.toPx() }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .pointerInput(selectedMonth) {
                var totalDragX = 0f

                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        totalDragX += dragAmount
                        if (abs(totalDragX) >= dragThresholdPx) {
                            if (totalDragX < 0f) {
                                onNextMonth()
                            } else {
                                onPreviousMonth()
                            }
                            totalDragX = 0f
                        }
                        change.consume()
                    },
                    onDragEnd = {
                        totalDragX = 0f
                    },
                    onDragCancel = {
                        totalDragX = 0f
                    }
                )
            }
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(
            text = "< ${selectedMonth.format(monthFormatter).replaceFirstChar { it.uppercaseChar() }} >",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
