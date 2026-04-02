package com.hazard.koe.presentation.accounts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.hazard.koe.R
import com.hazard.koe.data.enums.SupportedCurrency
import com.hazard.koe.presentation.accounts.UpcomingItem
import com.hazard.koe.presentation.subscriptions.SubscriptionIconCatalog
import com.hazard.koe.ui.theme.ExpenseRed
import java.time.format.DateTimeFormatter
import java.util.Locale

private val spanishDateFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es"))

@Composable
fun UpcomingPaymentsCard(
    items: List<UpcomingItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            when (item) {
                is UpcomingItem.CreditPayment -> CreditPaymentRow(item)
                is UpcomingItem.SubscriptionBilling -> SubscriptionBillingRow(item)
            }
        }
    }
}

@Composable
private fun CreditPaymentRow(
    payment: UpcomingItem.CreditPayment,
    modifier: Modifier = Modifier
) {
    val account = payment.account

    val accountColor = remember(account.color) {
        runCatching { Color(account.color.toColorInt()) }.getOrDefault(Color(0xFF1A73E8))
    }

    val dueDateText = remember(payment.dueDate) {
        spanishDateFormatter.format(payment.dueDate)
            .replaceFirstChar { it.uppercaseChar() }
    }

    val daysLabel = resolveDaysLabel(payment.daysRemaining)
    val daysColor = resolveDaysColor(payment.daysRemaining)

    val currencySymbol = remember(account.currencyCode) {
        SupportedCurrency.entries.find { it.code == account.currencyCode }?.symbol
            ?: account.currencyCode
    }

    val creditUsedFormatted = remember(account.creditUsed, currencySymbol) {
        val value = (account.creditUsed ?: 0L) / 100.0
        "$currencySymbol${String.format("%.2f", value)}"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(accountColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.calendar_fill),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = dueDateText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = daysLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = daysColor
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = creditUsedFormatted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SubscriptionBillingRow(
    billing: UpcomingItem.SubscriptionBilling,
    modifier: Modifier = Modifier
) {
    val circleColor = remember(billing.serviceColor) {
        billing.serviceColor?.let { hex ->
            runCatching { Color(hex.toColorInt()) }.getOrNull()
        } ?: Color(0xFF7C4DFF)
    }

    val iconSpec = remember(billing.serviceIconResName) {
        billing.serviceIconResName?.let { SubscriptionIconCatalog.forName(it) }
    }

    val dueDateText = remember(billing.dueDate) {
        spanishDateFormatter.format(billing.dueDate)
            .replaceFirstChar { it.uppercaseChar() }
    }

    val daysLabel = resolveDaysLabel(billing.daysRemaining)
    val daysColor = resolveDaysColor(billing.daysRemaining)

    val currencySymbol = remember(billing.currencyCode) {
        SupportedCurrency.entries.find { it.code == billing.currencyCode }?.symbol
            ?: billing.currencyCode
    }

    val amountFormatted = remember(billing.amount, currencySymbol) {
        val value = billing.amount / 100.0
        "$currencySymbol${String.format("%.2f", value)}"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(circleColor),
            contentAlignment = Alignment.Center
        ) {
            if (iconSpec != null) {
                Icon(
                    painter = painterResource(iconSpec.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(iconSpec.size),
                    tint = iconSpec.tint ?: Color.Unspecified
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.calendar_fill),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = billing.serviceName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = dueDateText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = daysLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = daysColor
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = amountFormatted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun resolveDaysLabel(daysRemaining: Int): String = when {
    daysRemaining < 0 -> "Vencido"
    daysRemaining == 0 -> "Hoy"
    daysRemaining == 1 -> "Manana"
    else -> "En $daysRemaining dias"
}

@Composable
private fun resolveDaysColor(daysRemaining: Int): Color = when {
    daysRemaining < 0 -> ExpenseRed
    daysRemaining == 0 -> Color(0xFFFFA726)
    daysRemaining == 1 -> Color(0xFFFFCA28)
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
