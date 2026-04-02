package com.hazard.koe.presentation.accounts.accountdetail.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hazard.koe.presentation.accounts.accountdetail.BalanceHistoryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max

enum class SparkBarPeriod(val label: String) {
    WEEK("Semana"),
    MONTH("Mes");

    val slotCount: Int
        get() = when (this) {
            WEEK -> 7
            MONTH -> 30
        }
}

data class BarSlot(
    val date: LocalDate,
    val balance: Long?,
    val hasData: Boolean
)

@Composable
fun SparkBarChart(
    balanceHistory: List<BalanceHistoryPoint>,
    currencyCode: String,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 120.dp,
    onSelectedBalanceChange: ((balance: Long?, deltaPercent: Float?) -> Unit)? = null
) {
    var selectedPeriod by remember { mutableStateOf(SparkBarPeriod.WEEK) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val today = remember { LocalDate.now() }

    val slots by remember(balanceHistory, selectedPeriod, today) {
        derivedStateOf {
            buildSlots(balanceHistory, selectedPeriod, today)
        }
    }

    // Reset selection when period changes
    LaunchedEffect(selectedPeriod, slots) {
        val lastDataIdx = slots.indexOfLast { it.hasData }
        selectedIndex = lastDataIdx
    }

    // Notify parent of selection changes
    LaunchedEffect(selectedIndex, slots) {
        if (selectedIndex in slots.indices && slots[selectedIndex].hasData) {
            val selected = slots[selectedIndex]
            val firstData = slots.firstOrNull { it.hasData }
            val delta = if (firstData != null && firstData.balance != null && firstData.balance != 0L && selected.balance != null) {
                ((selected.balance - firstData.balance).toFloat() / abs(firstData.balance).toFloat()) * 100f
            } else null
            onSelectedBalanceChange?.invoke(selected.balance, delta)
        } else {
            onSelectedBalanceChange?.invoke(null, null)
        }
    }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(selectedPeriod) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(400))
    }

    Column(modifier = modifier) {
        // Period tabs
        PeriodSelector(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = { selectedPeriod = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Bar chart canvas
        val primaryColor = MaterialTheme.colorScheme.primary
        val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
        val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        val errorColor = MaterialTheme.colorScheme.error
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        val density = LocalDensity.current

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .pointerInput(slots) {
                    detectTapGestures { offset ->
                        val n = slots.size
                        if (n == 0) return@detectTapGestures
                        val gap = if (n <= 7) 4.dp.toPx() else 1.5.dp.toPx()
                        val barW = (size.width - gap * (n - 1)) / n
                        val tappedIndex = (offset.x / (barW + gap)).toInt().coerceIn(0, n - 1)
                        if (slots[tappedIndex].hasData) {
                            selectedIndex = tappedIndex
                        }
                    }
                }
        ) {
            val n = slots.size
            if (n == 0) return@Canvas

            val dataSlots = slots.filter { it.hasData }
            if (dataSlots.isEmpty()) return@Canvas

            val gap = if (n <= 7) 4.dp.toPx() else 1.5.dp.toPx()
            val barW = (size.width - gap * (n - 1)) / n
            val topPad = 28.dp.toPx()

            val dataBalances = dataSlots.mapNotNull { it.balance }
            val minVal = minOf(dataBalances.min(), 0L)
            val maxVal = maxOf(dataBalances.max(), 0L)
            val hasNegative = minVal < 0L
            val absMax = maxOf(abs(minVal), abs(maxVal)).coerceAtLeast(1L)

            val posH: Float
            val negH: Float
            val zeroY: Float

            if (!hasNegative) {
                posH = size.height - topPad - 4.dp.toPx()
                negH = 0f
                zeroY = size.height - 2.dp.toPx()
            } else {
                val totalRange = (maxVal - minVal).coerceAtLeast(1L)
                val posRatio = maxVal.toFloat() / totalRange.toFloat()
                posH = max(20.dp.toPx(), (size.height - topPad - 8.dp.toPx()) * posRatio)
                negH = (size.height - topPad - 8.dp.toPx()) - posH
                zeroY = topPad + posH
            }

            // Zero line for negative balances
            if (hasNegative) {
                drawLine(
                    color = outlineColor,
                    start = Offset(0f, zeroY),
                    end = Offset(size.width, zeroY),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val cornerR = minOf(3.dp.toPx(), barW / 3f)
            val placeholderAlpha = 0.06f

            slots.forEachIndexed { i, slot ->
                val x = i * (barW + gap)
                val progress = animationProgress.value
                val isActive = i == selectedIndex

                if (!slot.hasData) {
                    val phH = 1.dp.toPx()
                    drawRoundRectBar(
                        x = x,
                        y = zeroY - phH,
                        width = barW,
                        height = phH,
                        topRadius = cornerR,
                        bottomRadius = 0f,
                        color = onSurfaceVariantColor.copy(alpha = 0.2f)
                    )
                    return@forEachIndexed
                }

                val balance = slot.balance ?: return@forEachIndexed

                if (balance >= 0) {
                    val targetH = max(3.dp.toPx(), (balance.toFloat() / absMax.toFloat()) * posH)
                    val h = targetH * progress
                    val barColor = if (isActive) primaryColor else onSurfaceVariantColor.copy(alpha = 0.3f)
                    drawRoundRectBar(
                        x = x,
                        y = zeroY - h,
                        width = barW,
                        height = h,
                        topRadius = cornerR,
                        bottomRadius = 0f,
                        color = barColor
                    )
                } else {
                    val targetH = max(3.dp.toPx(), (abs(balance).toFloat() / absMax.toFloat()) * negH)
                    val h = targetH * progress
                    val barColor = if (isActive) errorColor else onSurfaceVariantColor.copy(alpha = 0.3f)
                    drawRoundRectBar(
                        x = x,
                        y = zeroY,
                        width = barW,
                        height = h,
                        topRadius = 0f,
                        bottomRadius = cornerR,
                        color = barColor
                    )
                }

                // Tooltip for active bar
                if (isActive && balance != null) {
                    drawTooltip(
                        value = balance,
                        x = x,
                        barWidth = barW,
                        zeroY = zeroY,
                        posH = posH,
                        negH = negH,
                        absMax = absMax,
                        canvasWidth = size.width,
                        bgColor = if (balance < 0) errorColor else primaryColor,
                        density = density.density
                    )
                }
            }
        }

        // X-axis labels
        XAxisLabels(
            slots = slots,
            selectedPeriod = selectedPeriod,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: SparkBarPeriod,
    onPeriodSelected: (SparkBarPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            SparkBarPeriod.entries.forEach { period ->
                val isSelected = period == selectedPeriod
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        Color.Transparent
                    },
                    shadowElevation = if (isSelected) 1.dp else 0.dp,
                    onClick = { onPeriodSelected(period) }
                ) {
                    Text(
                        text = period.label,
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun XAxisLabels(
    slots: List<BarSlot>,
    selectedPeriod: SparkBarPeriod,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        if (selectedPeriod == SparkBarPeriod.WEEK) {
            slots.forEach { slot ->
                Text(
                    text = slot.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale("es")),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (slot.hasData) 1f else 0.35f
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            // Month: show every ~6th label
            val step = 6
            val indices = mutableListOf<Int>()
            var i = 0
            while (i < slots.size) {
                indices.add(i)
                i += step
            }
            if (indices.last() != slots.size - 1) {
                indices.add(slots.size - 1)
            }

            indices.forEach { idx ->
                val slot = slots[idx]
                Text(
                    text = "${slot.date.dayOfMonth}/${slot.date.monthValue}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (slot.hasData) 1f else 0.35f
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

private fun buildSlots(
    history: List<BalanceHistoryPoint>,
    period: SparkBarPeriod,
    today: LocalDate
): List<BarSlot> {
    val totalSlots = period.slotCount
    val zone = ZoneId.systemDefault()

    // Map history points by date
    val balanceByDate = history.associate { point ->
        Instant.ofEpochMilli(point.dateMillis)
            .atZone(zone)
            .toLocalDate() to point.balance
    }

    val dataCount = balanceByDate.size
    val isFilled = dataCount >= totalSlots

    return if (isFilled) {
        // Normal mode: today is the rightmost bar
        (0 until totalSlots).map { i ->
            val date = today.minusDays((totalSlots - 1 - i).toLong())
            BarSlot(
                date = date,
                balance = balanceByDate[date],
                hasData = balanceByDate.containsKey(date)
            )
        }
    } else {
        // Filling mode: data bars on the left, placeholders on the right
        val sortedDates = balanceByDate.keys.sorted()
        val startDate = sortedDates.firstOrNull() ?: today

        (0 until totalSlots).map { i ->
            val date = startDate.plusDays(i.toLong())
            val balance = balanceByDate[date]
            BarSlot(
                date = date,
                balance = balance,
                hasData = balance != null
            )
        }
    }
}

private fun DrawScope.drawRoundRectBar(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    topRadius: Float,
    bottomRadius: Float,
    color: Color
) {
    if (height <= 0f) return
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                left = x,
                top = y,
                right = x + width,
                bottom = y + height,
                topLeftCornerRadius = CornerRadius(topRadius),
                topRightCornerRadius = CornerRadius(topRadius),
                bottomLeftCornerRadius = CornerRadius(bottomRadius),
                bottomRightCornerRadius = CornerRadius(bottomRadius)
            )
        )
    }
    drawPath(path, color)
}

private fun DrawScope.drawTooltip(
    value: Long,
    x: Float,
    barWidth: Float,
    zeroY: Float,
    posH: Float,
    negH: Float,
    absMax: Long,
    canvasWidth: Float,
    bgColor: Color,
    density: Float
) {
    val text = formatCompact(value)

    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 11f * density
        typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
    }

    val textWidth = paint.measureText(text)
    val paddingH = 12f * density
    val pillW = textWidth + paddingH
    val pillH = 20f * density
    val arrowH = 5f * density
    val cornerR = 6f * density

    val barCenterX = x + barWidth / 2f

    var pillX = barCenterX - pillW / 2f
    pillX = pillX.coerceIn(0f, canvasWidth - pillW)

    val barTop = if (value >= 0) {
        zeroY - max(3 * density, (value.toFloat() / absMax.toFloat()) * posH)
    } else {
        zeroY
    }

    var pillY = barTop - pillH - arrowH - 4f * density
    pillY = pillY.coerceAtLeast(0f)

    // Pill background
    drawRoundRectBar(
        x = pillX,
        y = pillY,
        width = pillW,
        height = pillH,
        topRadius = cornerR,
        bottomRadius = cornerR,
        color = bgColor
    )

    // Arrow
    val arrowPath = Path().apply {
        moveTo(barCenterX - 4f * density, pillY + pillH)
        lineTo(barCenterX + 4f * density, pillY + pillH)
        lineTo(barCenterX, pillY + pillH + arrowH)
        close()
    }
    drawPath(arrowPath, bgColor)

    // Text
    drawContext.canvas.nativeCanvas.drawText(
        text,
        pillX + pillW / 2f,
        pillY + pillH / 2f + paint.textSize / 3f,
        paint
    )
}

private fun formatCompact(value: Long): String {
    val decimal = value / 100.0
    val sign = if (decimal < 0) "-" else ""
    val absDecimal = abs(decimal)
    val formatted = when {
        absDecimal >= 1_000_000 -> String.format("%.1fM", absDecimal / 1_000_000.0)
        absDecimal >= 1_000 -> String.format("%.1fK", absDecimal / 1_000.0)
        else -> String.format("%.2f", absDecimal)
    }
    return "$sign$formatted"
}