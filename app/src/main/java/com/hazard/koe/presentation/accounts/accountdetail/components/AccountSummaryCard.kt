package com.hazard.koe.presentation.accounts.accountdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.enums.SupportedCurrency
import com.hazard.koe.data.model.Account

private fun formatBalance(amountMinor: Long, currencyCode: String): String {
    val symbol = SupportedCurrency.entries
        .find { it.code == currencyCode }?.symbol ?: currencyCode
    val value = amountMinor / 100.0
    return "$symbol${String.format("%.2f", value)}"
}

private fun parseColor(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(Color(0xFF1A73E8))

@Composable
fun AccountSummaryCard(
    account: Account,
    modifier: Modifier = Modifier
) {
    val accountColor = parseColor(account.color)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accountColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = account.name,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Text(
                text = account.type.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Balance",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = formatBalance(account.currentBalance, account.currencyCode),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            if (account.type == AccountType.CREDIT) {
                val limit = account.creditLimit ?: 0L
                val used = account.creditUsed ?: 0L
                val available = (limit - used).coerceAtLeast(0L)
                val progress = if (limit > 0L) used.toFloat() / limit.toFloat() else 0f

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Used",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatBalance(used, account.currencyCode),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "Available",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatBalance(available, account.currencyCode),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "Limit",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatBalance(limit, account.currencyCode),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                ) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}
