package com.example.tracker.presentation.accounts.accountdetail.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracker.presentation.accounts.accountdetail.BalanceHistoryPoint
import com.example.tracker.presentation.util.CurrencyFormatter
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val chartDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm")

@Composable
fun BalanceChart(
    history: List<BalanceHistoryPoint>,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    if (history.size < 2) return

    InteractiveLineChart(
        data = history.map { it.balanceMinor / 100f },
        activeColor = MaterialTheme.colorScheme.onSurface,
        inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant,
        indicatorLineColor = MaterialTheme.colorScheme.outline,
        strokeWidth = 12f,
        modifier = modifier,
        labels = { index, _ ->
            val point = history[index]
            point.dateTime.format(chartDateFormatter) to
                    CurrencyFormatter.formatBalance(point.balanceMinor, currencyCode)
        }
    )
}

@Composable
fun InteractiveLineChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF1D9E75),
    inactiveColor: Color = Color(0xFFB4B2A9),
    strokeWidth: Float = 12f,
    dotRadius: Float = 18f,
    dotBorderWidth: Float = 6f,
    indicatorLineWidth: Float = 3f,
    indicatorLineColor: Color = Color(0xFFD3D1C7),
    labels: (index: Int, value: Float) -> Pair<String, String> = { _, value ->
        // Par: primera línea (tiempo/label), segunda línea (monto)
        "" to "$${"%,.2f".format(value)}"
    }
) {
    if (data.size < 2) return

    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var activeIndex by remember { mutableIntStateOf(-1) }
    var isDragging by remember { mutableStateOf(false) }

    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animSpec())
    }

    val rawMinValue = remember(data) { data.min() }
    val rawMaxValue = remember(data) { data.max() }

    // Calculate Y-axis range with breathing room (padding)
    // Top: 20% above the maximum balance value
    // Bottom: 0 if min >= 0, else 20% below the minimum (more negative)
    val yMin = remember(rawMinValue, rawMaxValue) {
        if (rawMinValue >= 0f) 0f
        else rawMinValue - (rawMaxValue - rawMinValue).coerceAtLeast(0.01f) * 0.20f
    }
    val yMax = remember(rawMinValue, rawMaxValue) {
        rawMaxValue + (rawMaxValue - rawMinValue).coerceAtLeast(0.01f) * 0.20f
    }
    val valueRange = remember(yMin, yMax) { yMax - yMin }

    // Mapeamos cada dato a coordenadas de canvas
    // Use extra top padding to prevent clipping when bezier curves extend beyond points
    val chartPaddingTop = (dotRadius + dotBorderWidth) * 2 // extra space for curve overshoot
    val tooltipTopPad = 60f + chartPaddingTop // espacio para la pill del tooltip arriba + curve padding
    val padBottom = dotRadius + dotBorderWidth

    val coords by remember(data, canvasSize) {
        derivedStateOf {
            if (canvasSize.width == 0 || canvasSize.height == 0) {
                emptyList()
            } else {
                val padH = dotRadius + dotBorderWidth
                val w = canvasSize.width - padH * 2
                val h = canvasSize.height - tooltipTopPad - padBottom
                data.mapIndexed { i, v ->
                    Offset(
                        x = padH + (i.toFloat() / (data.lastIndex)) * w,
                        y = tooltipTopPad + h - ((v - yMin) / valueRange) * h
                    )
                }
            }
        }
    }

    fun closestIndex(x: Float): Int {
        if (coords.isEmpty()) return -1
        var closest = 0
        var minDist = Float.MAX_VALUE
        for (i in coords.indices) {
            val d = kotlin.math.abs(coords[i].x - x)
            if (d < minDist) {
                minDist = d
                closest = i
            }
        }
        return closest
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .onSizeChanged { canvasSize = it }
                .pointerInput(data) {
                    awaitPointerEventScope {
                        while (true) {
                            // Esperar el primer toque
                            val down = awaitFirstDown(requireUnconsumed = false)
                            activeIndex = closestIndex(down.position.x)
                            isDragging = true
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                            // Follow drag with haptic feedback
                            var lastHapticIndex = activeIndex
                            do {
                                val event = awaitPointerEvent()
                                val pointer = event.changes.firstOrNull() ?: break
                                pointer.consume()
                                val newIndex = closestIndex(pointer.position.x)
                                if (newIndex != lastHapticIndex) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    lastHapticIndex = newIndex
                                }
                                activeIndex = newIndex
                            } while (event.changes.any { it.pressed })

                            isDragging = false
                        }
                    }
                }
        ) {
            if (coords.size < 2) return@Canvas

            val progress = animProgress.value
            val drawCount = (coords.size * progress).roundToInt().coerceAtLeast(2)
            val visible = coords.take(drawCount)

            // Construir UN solo path completo con todas las curvas
            val fullPath = buildSmoothPath(visible, 0, visible.lastIndex)

            val splitIdx = if (activeIndex in visible.indices) activeIndex
            else visible.lastIndex

            val splitX = visible[splitIdx].x

            // Calculate chart area bounds to prevent clipping
            // These match the coordinate calculation: yMin at bottom, yMax at top
            val chartTop = tooltipTopPad - chartPaddingTop
            val chartBottom = size.height - padBottom + chartPaddingTop

            // Dibujar el path activo (izquierda) con clip
            clipRect(
                left = 0f,
                top = chartTop,
                right = splitX,
                bottom = chartBottom
            ) {
                drawPath(
                    path = fullPath,
                    color = activeColor,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // Dibujar el path inactivo (derecha) con clip
            if (splitIdx < visible.lastIndex) {
                clipRect(
                    left = splitX,
                    top = chartTop,
                    right = size.width,
                    bottom = chartBottom
                ) {
                    drawPath(
                        path = fullPath,
                        color = inactiveColor,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Línea vertical indicadora (todo el alto del canvas)
            if (activeIndex in visible.indices) {
                val point = visible[activeIndex]

                drawLine(
                    color = indicatorLineColor,
                    start = Offset(point.x, 0f),
                    end = Offset(point.x, size.height),
                    strokeWidth = indicatorLineWidth,
                    cap = StrokeCap.Round
                )

                // Punto sobre la curva
                drawCircle(
                    color = activeColor,
                    radius = dotRadius,
                    center = point
                )
                drawCircle(
                    color = inactiveColor,
                    radius = dotRadius - dotBorderWidth,
                    center = point
                )
            }
        }

        // Tooltip pill en la parte superior de la línea indicadora
        if (activeIndex in coords.indices && coords.isNotEmpty()) {
            val point = coords[activeIndex]
            val (label, amount) = labels(activeIndex, data[activeIndex])
            val tooltipWidthDp = 120.dp
            val tooltipWidthPx = with(density) { tooltipWidthDp.toPx() }

            val clampedX = point.x
                .coerceAtLeast(tooltipWidthPx / 2)
                .coerceAtMost((canvasSize.width - tooltipWidthPx / 2).coerceAtLeast(0f))

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (clampedX - tooltipWidthPx / 2).roundToInt(),
                            y = 0
                        )
                    }
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (label.isNotEmpty()) {
                            Text(
                                text = label,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                lineHeight = 10.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = amount,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 11.sp,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Construye un Path suavizado entre los puntos [fromIdx] y [toIdx].
 * Usa curvas cúbicas de Bézier con tension para suavizar las esquinas.
 */
private fun buildSmoothPath(
    points: List<Offset>,
    fromIdx: Int,
    toIdx: Int,
    tension: Float = 0.3f
): Path {
    val path = Path()
    if (fromIdx >= toIdx || fromIdx !in points.indices || toIdx !in points.indices) return path

    path.moveTo(points[fromIdx].x, points[fromIdx].y)

    for (i in fromIdx until toIdx) {
        val p0 = points[(i - 1).coerceAtLeast(fromIdx)]
        val p1 = points[i]
        val p2 = points[i + 1]
        val p3 = points[(i + 2).coerceAtMost(toIdx)]

        val cp1x = p1.x + (p2.x - p0.x) * tension
        val cp1y = p1.y + (p2.y - p0.y) * tension
        val cp2x = p2.x - (p3.x - p1.x) * tension
        val cp2y = p2.y - (p3.y - p1.y) * tension

        path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
    }

    return path
}

private fun animSpec() = tween<Float>(durationMillis = 800)