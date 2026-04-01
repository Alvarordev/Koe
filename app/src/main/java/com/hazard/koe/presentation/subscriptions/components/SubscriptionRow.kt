package com.hazard.koe.presentation.subscriptions.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hazard.koe.data.model.relations.SubscriptionWithDetails
import com.hazard.koe.presentation.components.EmojiText
import com.hazard.koe.presentation.subscriptions.SubscriptionIconCatalog
import com.hazard.koe.presentation.util.CurrencyFormatter

@Composable
fun SubscriptionRow(
    subscription: SubscriptionWithDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sub = subscription.subscription
    val service = subscription.service
    val account = subscription.account
    val iconSpec = (sub.iconResName ?: service?.iconResName)?.let { SubscriptionIconCatalog.forName(it) }
    val displayName = sub.customName ?: service?.name ?: ""
    val currencyCode = account?.currencyCode ?: sub.currencyCode
    val accountName = account?.name ?: ""

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(48.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (iconSpec != null) {
                    Icon(
                        painter = painterResource(id = iconSpec.iconRes),
                        contentDescription = displayName,
                        modifier = Modifier.size(iconSpec.size),
                        tint = iconSpec.tint ?: androidx.compose.ui.graphics.Color.Unspecified
                    )
                } else {
                    EmojiText(
                        text = sub.customEmoji ?: "",
                        style = TextStyle(fontSize = 20.sp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp ))
            Text(
                text = "Día ${sub.billingDay}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = CurrencyFormatter.formatBalance(sub.amount, currencyCode),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp ))
            Text(
                text = accountName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
