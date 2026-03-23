package com.example.tracker.presentation.categories.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.tracker.data.model.SubscriptionService

private fun parseColor(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(Color(0xFF1A73E8))

@Composable
fun SubscriptionServiceItem(
    service: SubscriptionService,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val iconResId = runCatching {
        context.resources.getIdentifier(service.iconResName, "drawable", context.packageName)
    }.getOrDefault(0)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconResId != 0) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = service.name,
                modifier = Modifier.size(32.dp),
                tint = Color.Unspecified
            )
        } else {
            Icon(
                imageVector = Icons.Default.Subscriptions,
                contentDescription = service.name,
                modifier = Modifier.size(32.dp),
                tint = parseColor(service.color)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = service.name,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(parseColor(service.color), CircleShape)
        )
    }
}
