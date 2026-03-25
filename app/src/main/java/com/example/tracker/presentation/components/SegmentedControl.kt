package com.example.tracker.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableIntStateOf

@Composable
fun SegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    activeColor: Color,
    activeTextColor: Color,
    inactiveColor: Color,
    inactiveTextColor: Color,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val containerWidthPx = remember { mutableIntStateOf(0) }
    val segmentCount = items.size.coerceAtLeast(1)
    val segmentWidthDp: Dp = with(density) { (containerWidthPx.intValue / segmentCount).toDp() }
    val pillPadding = 4.dp
    val pillWidth = segmentWidthDp - pillPadding

    val pillOffset by animateDpAsState(
        targetValue = pillPadding / 2 + segmentWidthDp * selectedIndex,
        animationSpec = tween(durationMillis = 250),
        label = "pillOffset"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(inactiveColor)
            .padding(vertical = 4.dp)
            .onSizeChanged { containerWidthPx.intValue = it.width }
    ) {
        if (segmentWidthDp > 0.dp) {
            Box(
                modifier = Modifier
                    .offset(x = pillOffset)
                    .width(pillWidth)
                    .height(36.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(activeColor)
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onItemSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (index == selectedIndex) activeTextColor else inactiveTextColor
                    )
                }
            }
        }
    }
}
