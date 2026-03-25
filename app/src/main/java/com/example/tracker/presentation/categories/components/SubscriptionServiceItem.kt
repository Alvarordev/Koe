package com.example.tracker.presentation.categories.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracker.data.model.relations.RecurringRuleWithDetails

private fun parseColor(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(Color(0xFF1A73E8))

@Composable
fun SubscriptionServiceItem(
    ruleWithDetails: RecurringRuleWithDetails,
    modifier: Modifier = Modifier
) {
    val service = ruleWithDetails.subscriptionService
    val rule = ruleWithDetails.rule
    val context = LocalContext.current

    val iconResId = service?.let {
        runCatching {
            context.resources.getIdentifier(it.iconResName, "drawable", context.packageName)
        }.getOrDefault(0)
    } ?: 0

    val serviceColor = service?.color ?: "#1A73E8"
    val displayName = service?.name ?: rule.name

    val amount = rule.amount
    val value = amount / 100.0
    val formattedAmount = if (amount % 100 == 0L) {
        String.format("%.0f", value)
    } else {
        String.format("%.2f", value)
    }

    val dayOfMonth = rule.dayOfMonth

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconResId != 0) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = displayName,
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(parseColor(serviceColor), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Subscriptions,
                    contentDescription = displayName,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (dayOfMonth != null) {
                Text(
                    text = "Vence el $dayOfMonth",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = formattedAmount,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
