package com.hazard.koe.presentation.components

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.Category
import com.hazard.koe.presentation.accounts.components.AccountCard

private val filterPillShape = RoundedCornerShape(20.dp)
private val filterPillEasing = CubicBezierEasing(0.2f, 0.9f, 0.25f, 1f)

fun filterPillEnterTransition(): EnterTransition {
    return fadeIn(animationSpec = tween(durationMillis = 320, easing = filterPillEasing)) +
            scaleIn(
                initialScale = 0.72f,
                animationSpec = tween(durationMillis = 320, easing = filterPillEasing)
            )
}

fun filterPillExitTransition(): ExitTransition {
    return fadeOut(animationSpec = tween(durationMillis = 220, easing = filterPillEasing)) +
            scaleOut(
                targetScale = 0.72f,
                animationSpec = tween(durationMillis = 220, easing = filterPillEasing)
            )
}

@Composable
fun CategoryFilterPill(
    category: Category,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = parseColor(category.color)
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = filterPillShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .border(width = 1.dp, color = borderColor, shape = filterPillShape)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            EmojiText(
                text = category.emoji,
                color = categoryColor,
                style = TextStyle(fontSize = 12.sp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = categoryColor
            )
        }
    }
}

@Composable
fun AccountFilterPill(
    account: Account,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = filterPillShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .border(width = 1.dp, color = borderColor, shape = filterPillShape)
                .padding(start = 10.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
        ) {
            AccountCard(
                account = account,
                cardHeight = 14.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = account.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun parseColor(hex: String): Color = runCatching {
    Color(hex.toColorInt())
}.getOrDefault(Color(0xFF1A73E8))
