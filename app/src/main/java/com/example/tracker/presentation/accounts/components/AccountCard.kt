package com.example.tracker.presentation.accounts.components

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tracker.data.enums.AccountType
import com.example.tracker.data.model.Account

private fun accountTypeLabel(type: AccountType): String = when (type) {
    AccountType.CASH -> "Cash"
    AccountType.DEBIT -> "Debit"
    AccountType.CREDIT -> "Credit"
    AccountType.SAVINGS -> "Savings"
}

private fun parseColor(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(Color(0xFF1A73E8))

private fun formatBalance(amountMinor: Long, currencyCode: String): String {
    val symbol = com.example.tracker.data.enums.SupportedCurrency.entries
        .find { it.code == currencyCode }?.symbol ?: currencyCode
    val value = amountMinor / 100.0
    return "$symbol${String.format("%.2f", value)}"
}

@Composable
fun AccountCard(
    account: Account,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accountColor = parseColor(account.color)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accountColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = account.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = accountColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = accountTypeLabel(account.type),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatBalance(account.currentBalance, account.currencyCode),
                style = MaterialTheme.typography.bodyLarge,
                color = if (account.currentBalance >= 0)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.error
            )
        }
    }
}
