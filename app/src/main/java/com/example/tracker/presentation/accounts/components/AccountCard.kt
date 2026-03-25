package com.example.tracker.presentation.accounts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracker.data.enums.AccountType
import com.example.tracker.data.enums.CardNetwork
import com.example.tracker.data.enums.SupportedCurrency
import com.example.tracker.data.model.Account
import java.text.NumberFormat
import java.util.Locale
import androidx.core.graphics.toColorInt

private fun parseColor(hex: String): Color = runCatching {
    Color(hex.toColorInt())
}.getOrDefault(Color(0xFF1A73E8))

@Composable
fun AccountCard(
    account: Account,
    modifier: Modifier = Modifier,
    scaleFactor: Float = 1f,
    onClick: () -> Unit = {}
) {
    val cardColor = parseColor(account.color)
    val s = scaleFactor
    val currencySymbol = SupportedCurrency.entries
        .find { it.code == account.currencyCode }?.symbol ?: account.currencyCode

    val backgroundGradient = remember(cardColor) {
        Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to cardColor,
                0.7f to cardColor.reduceLightness(0.1f),
                1.0f to cardColor.reduceLightness(0.2f)
            )
        )
    }

    val displayBalance = when (account.type) {
        AccountType.CREDIT -> {
            val available = (account.creditLimit ?: 0L) - (account.creditUsed ?: 0L)
            available / 100.0
        }
        else -> account.currentBalance / 100.0
    }

    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.58f)
            .clip(RoundedCornerShape((24 * s).dp))
            .background(backgroundGradient)
            .clickable(onClick = onClick)
            .clipToBounds()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding((24 * s).dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = account.name,
                    color = Color.White,
                    fontSize = (18 * s).sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = (20 * s).sp
                )

                when {
                    account.type == AccountType.CASH -> {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Efectivo",
                            tint = Color.White,
                            modifier = Modifier.size((28 * s).dp)
                        )
                    }
                    account.cardNetwork != null -> {
                        CardNetworkIcon(network = account.cardNetwork, scaleFactor = s)
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size((28 * s).dp)
                        )
                    }
                }
            }

            Column {
                if (account.type == AccountType.CREDIT) {
                    Text(
                        text = "Disponible",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = (12 * s).sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = (14 * s).sp
                    )
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = currencySymbol,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = (16 * s).sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = (18 * s).sp
                    )

                    Spacer(modifier = Modifier.width((6 * s).dp))

                    Text(
                        text = formatter.format(displayBalance),
                        color = Color.White,
                        fontSize = (28 * s).sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (1 * s).sp,
                        lineHeight = (32 * s).sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((16 * s).dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(bottom = (4 * s).dp)
                ) {
                    val typeLabel = when (account.type) {
                        AccountType.CASH -> "Efectivo"
                        AccountType.DEBIT -> "Débito"
                        AccountType.CREDIT -> "Crédito"
                        AccountType.SAVINGS -> "Ahorros"
                    }
                    Text(
                        text = typeLabel,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = (14 * s).sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = (16 * s).sp
                    )
                }

                if (account.lastFourDigits != null) {
                    Text(
                        text = "**** ${account.lastFourDigits}",
                        color = Color.White,
                        fontSize = (18 * s).sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (2 * s).sp,
                        lineHeight = (20 * s).sp
                    )
                }
            }
        }
    }
}

@Composable
fun CardNetworkIcon(network: CardNetwork, modifier: Modifier = Modifier, scaleFactor: Float = 1f) {
    val s = scaleFactor
    when (network) {
        CardNetwork.MASTERCARD -> {
            Box(
                modifier = modifier
                    .width((42 * s).dp)
                    .height((26 * s).dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size((26 * s).dp)
                        .align(Alignment.CenterStart)
                        .background(Color.White, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size((26 * s).dp)
                        .align(Alignment.CenterEnd)
                        .border((1 * s).dp, Color.White, CircleShape)
                )
            }
        }
        CardNetwork.VISA -> {
            Text(
                text = "VISA",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (20 * s).sp,
                lineHeight = (22 * s).sp,
                modifier = modifier
            )
        }
        CardNetwork.AMEX -> {
            Text(
                text = "AMEX",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (16 * s).sp,
                lineHeight = (18 * s).sp,
                modifier = modifier
            )
        }
        else -> {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = Color.White,
                modifier = modifier.size((28 * s).dp)
            )
        }
    }
}

private fun Color.reduceLightness(factor: Float): Color {
    val r = (red * (1 - factor)).coerceIn(0f, 1f)
    val g = (green * (1 - factor)).coerceIn(0f, 1f)
    val b = (blue * (1 - factor)).coerceIn(0f, 1f)
    return Color(r, g, b, alpha)
}
