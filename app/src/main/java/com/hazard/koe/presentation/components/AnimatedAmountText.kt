package com.hazard.koe.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

private enum class SlotState { IDLE, ENTERING, EXITING }

private class CharSlot(
    val char: Char,
    val key: String = UUID.randomUUID().toString(),
    val state: SlotState = SlotState.IDLE
)

@Composable
fun AnimatedAmountText(
    text: String,
    maxFontSize: TextUnit = 80.sp,
    minFontSize: TextUnit = 42.sp,
    shrinkThreshold: Int = 4,
    fontWeight: FontWeight = FontWeight.SemiBold,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign = TextAlign.Center,
    modifier: Modifier = Modifier,
    animationDurationMs: Int = 200,
) {
    val slots = remember { mutableStateListOf<CharSlot>() }

    // Ref que NO causa recomposición — solo se lee dentro de buildSlots
    val prevTextRef = remember { mutableStateOf("") }

    // Flag para primera composición
    val isFirstComposition = remember { mutableStateOf(true) }

    // Contar solo chars activos (no los que están saliendo)
    val activeCharCount by remember {
        derivedStateOf { slots.count { it.state != SlotState.EXITING } }
    }

    val maxSpValue = maxFontSize.value
    val minSpValue = minFontSize.value


    // Calcular target: de threshold en adelante, cada char extra reduce proporcionalmente
    // hasta un máximo razonable de chars (ej: 10) donde llega al mínimo
    val maxCharsForMinSize = shrinkThreshold + 6 // a los 10 chars llega al mínimo
    val targetFontSizeSp = if (activeCharCount <= shrinkThreshold) {
        maxSpValue
    } else {
        val progress = ((activeCharCount - shrinkThreshold).toFloat() / (maxCharsForMinSize - shrinkThreshold))
            .coerceIn(0f, 1f)
        maxSpValue - (maxSpValue - minSpValue) * progress
    }

    val animatedFontSizeSp by animateFloatAsState(
        targetValue = targetFontSizeSp,
        animationSpec = tween(durationMillis = animationDurationMs, easing = EaseOutCubic),
        label = "fontSizeAnim"
    )
    val animatedFontSize = animatedFontSizeSp.sp

    // Función pura que calcula los nuevos slots
    fun buildSlots(oldText: String, newText: String, currentSlots: List<CharSlot>): List<CharSlot> {
        val oldChars = oldText.toList()
        val newChars = newText.toList()
        val result = mutableListOf<CharSlot>()

        for (i in newChars.indices) {
            if (i < oldChars.size && newChars[i] == oldChars[i]) {
                // Reusar slot existente (que no sea EXITING)
                val existing = currentSlots.getOrNull(i)
                if (existing != null && existing.state != SlotState.EXITING) {
                    result.add(CharSlot(char = newChars[i], key = existing.key, state = SlotState.IDLE))
                } else {
                    result.add(CharSlot(char = newChars[i], state = SlotState.IDLE))
                }
            } else {
                // Nuevo o cambiado
                result.add(CharSlot(char = newChars[i], state = SlotState.ENTERING))
            }
        }

        // Chars eliminados
        if (oldChars.size > newChars.size) {
            for (i in newChars.size until oldChars.size) {
                result.add(CharSlot(char = oldChars[i], state = SlotState.EXITING))
            }
        }

        return result
    }

    // Reaccionar a cambios de text
    LaunchedEffect(text) {
        val oldText = prevTextRef.value

        if (isFirstComposition.value) {
            // Primera vez: todos ENTERING
            isFirstComposition.value = false
            if (text.isNotEmpty()) {
                slots.clear()
                slots.addAll(text.map { c -> CharSlot(char = c, state = SlotState.ENTERING) })
            }
        } else if (oldText != text) {
            // Filtrar los EXITING que aún estén animando — mantenerlos no
            // porque van a ser reemplazados por el nuevo diff
            val activeSlots = slots.filter { it.state != SlotState.EXITING }
            val newSlots = buildSlots(oldText, text, activeSlots)
            slots.clear()
            slots.addAll(newSlots)
        }

        prevTextRef.value = text
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        // Snapshot inmutable para iterar
        val currentSlots = slots.toList()
        currentSlots.forEach { slot ->
            key(slot.key) {
                AnimatedCharSlot(
                    slot = slot,
                    fontSize = animatedFontSize,
                    fontWeight = fontWeight,
                    color = color,
                    animationDurationMs = animationDurationMs,
                    onExitComplete = {
                        slots.removeAll { s -> s.key == slot.key }
                    }
                )
            }
        }
    }
}


@Composable
private fun AnimatedCharSlot(
    slot: CharSlot,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    color: Color,
    animationDurationMs: Int,
    onExitComplete: () -> Unit
) {
    val density = LocalDensity.current
    var slotHeightPx by remember { mutableStateOf(0) }
    var slotWidthPx by remember { mutableStateOf(0) }
    var measuredOnce by remember { mutableStateOf(false) }

    val offsetFraction = remember {
        Animatable(
            when (slot.state) {
                SlotState.ENTERING -> 1f
                SlotState.EXITING -> 0f
                SlotState.IDLE -> 0f
            }
        )
    }
    val widthFraction = remember { Animatable(1f) }

    // Se ejecuta UNA sola vez por slot (key única)
    LaunchedEffect(Unit) {
        when (slot.state) {
            SlotState.ENTERING -> {
                offsetFraction.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = animationDurationMs,
                        easing = EaseOutCubic
                    )
                )
            }
            SlotState.EXITING -> {
                coroutineScope {
                    launch {
                        offsetFraction.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = animationDurationMs,
                                easing = EaseInCubic
                            )
                        )
                    }
                    launch {
                        delay((animationDurationMs * 0.3).toLong())
                        widthFraction.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(
                                durationMillis = (animationDurationMs * 0.7).toInt(),
                                easing = EaseInCubic
                            )
                        )
                    }
                }
                onExitComplete()
            }
            SlotState.IDLE -> {}
        }
    }

    val widthModifier = if (slot.state == SlotState.EXITING && measuredOnce) {
        Modifier.width(with(density) { (slotWidthPx * widthFraction.value).toDp() })
    } else {
        Modifier
    }

    Box(
        modifier = widthModifier
            .clipToBounds()
            .onSizeChanged { size ->
                slotHeightPx = size.height
                if (!measuredOnce) {
                    slotWidthPx = size.width
                    measuredOnce = true
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = slot.char.toString(),
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            textAlign = TextAlign.Center,
            modifier = Modifier.offset {
                IntOffset(
                    x = 0,
                    y = (slotHeightPx * offsetFraction.value).toInt()
                )
            }
        )
    }
}
