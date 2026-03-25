package com.example.tracker.presentation.components

import android.util.TypedValue
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    val resolver = LocalFontFamilyResolver.current
    val density = LocalDensity.current

    val calculatedEmojiSize = remember(style.fontSize, emojiSizeMultiplier) {
        with(density) {
            (style.fontSize.toPx() * emojiSizeMultiplier).toInt()
        }
    }

    val typeface: android.graphics.Typeface = remember(style) {
        resolver.resolve(
            fontFamily = style.fontFamily,
            fontWeight = style.fontWeight ?: FontWeight.Normal,
            fontStyle = style.fontStyle ?: FontStyle.Normal,
            fontSynthesis = FontSynthesis.All
        ).value as android.graphics.Typeface
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
            view.setTypeface(typeface)
            // Es vital pasarle la unidad SP explícitamente al TextView nativo
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.fontSize.value)

            // Seteamos el tamaño calculado en píxeles
            view.setEmojiSize(calculatedEmojiSize)

            textColor?.let { view.setTextColor(it) }
            view.text = text
        }
    )
}