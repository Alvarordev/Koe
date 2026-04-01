package com.hazard.koe.presentation.components

import android.util.TypedValue
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import com.vanniktech.emoji.EmojiTextView

@Composable
fun EmojiText(
    text: String?,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    emojiSizeMultiplier: Float = 1.2f
) {
    var showFallback by remember { mutableStateOf(false) }

    LaunchedEffect(text, style) {
        showFallback = false
    }

    if (showFallback) {
        Text(
            text = text ?: "",
            modifier = modifier,
            style = style,
            color = color
        )
        return
    }

    EmojiTextCore(
        text = text,
        modifier = modifier,
        style = style,
        color = color,
        emojiSizeMultiplier = emojiSizeMultiplier,
        onError = { showFallback = true }
    )
}

@Composable
private fun EmojiTextCore(
    text: String?,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    emojiSizeMultiplier: Float = 1.2f,
    onError: () -> Unit
) {
    val resolver = LocalFontFamilyResolver.current
    val density = LocalDensity.current

    val calculatedEmojiSize = remember(style.fontSize, emojiSizeMultiplier) {
        with(density) {
            (style.fontSize.toPx() * emojiSizeMultiplier).toInt()
        }
    }

    val typeface = remember(style) {
        try {
            resolver.resolve(
                fontFamily = style.fontFamily,
                fontWeight = style.fontWeight ?: FontWeight.Normal,
                fontStyle = style.fontStyle ?: FontStyle.Normal,
                fontSynthesis = FontSynthesis.All
            ).value as android.graphics.Typeface
        } catch (e: Exception) {
            onError()
            null
        }
    }

    if (typeface == null) {
        Text(
            text = text ?: "",
            modifier = modifier,
            style = style,
            color = color
        )
        return
    }

    val textColor = if (color != Color.Unspecified) color.toArgb() else null

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            EmojiTextView(ctx).apply {
                gravity = android.view.Gravity.CENTER
            }
        },
        update = { view ->
            try {
                view.setTypeface(typeface)
                view.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.fontSize.value)
                view.setEmojiSize(calculatedEmojiSize)
                textColor?.let { view.setTextColor(it) }
                view.text = text
            } catch (e: Exception) {
                // Silent fail during update
            }
        }
    )
}