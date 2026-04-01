package com.example.tracker.presentation.categories.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.tracker.data.model.Category
import com.example.tracker.data.model.relations.CategoryIdSummary
import com.example.tracker.presentation.components.EmojiText

@Composable
fun CategoryGridCard(
    category: Category,
    summary: CategoryIdSummary?,
    currencyCode: String = "PEN",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = try {
        Color(category.color.toColorInt())
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }
    val total = summary?.total ?: 0L
    val value = total / 100.0
    val formattedAmount = if (total % 100 == 0L) {
        String.format("%.0f", value)
    } else {
        String.format("%.2f", value)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(19.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(19.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color = categoryColor),
            contentAlignment = Alignment.Center
        ) {
            EmojiText(
                text = category.emoji,
                style = TextStyle(fontSize = 20.sp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$formattedAmount $currencyCode",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
