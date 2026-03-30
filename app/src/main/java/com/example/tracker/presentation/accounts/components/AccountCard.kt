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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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

// Altura base de referencia (dp) sobre la cual se calculan todas las proporciones
private const val BASE_CARD_HEIGHT_DP = 220f
private const val CARD_ASPECT_RATIO = 1.58f

@Composable
fun AccountCard(
    account: Account,
    modifier: Modifier = Modifier,
    cardHeight: Dp = BASE_CARD_HEIGHT_DP.dp,
    onClick: () -> Unit = {}
) {
    val density = LocalDensity.current
    val s = remember(cardHeight) {
        with(density) { cardHeight.toPx() } /
                with(density) { BASE_CARD_HEIGHT_DP.dp.toPx() }
    }

    val cardColor = parseColor(account.color)
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

    val formatter = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
    }

    Box(
        modifier = modifier
            .height(cardHeight)
            .aspectRatio(CARD_ASPECT_RATIO)
            .clip(RoundedCornerShape(scaled(24, s)))
            .background(backgroundGradient)
            .clickable(onClick = onClick)
            .clipToBounds()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaled(24, s)),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- Top row: nombre + icono ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = account.name,
                    color = Color.White,
                    fontSize = scaledSp(18, s),
                    fontWeight = FontWeight.Medium,
                    lineHeight = scaledSp(20, s)
                )

                AccountCardIcon(account = account, scaleFactor = s)
            }

            // --- Centro: balance ---
            Column {
                if (account.type == AccountType.CREDIT) {
                    Text(
                        text = "Disponible",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = scaledSp(12, s),
                        fontWeight = FontWeight.Normal,
                        lineHeight = scaledSp(14, s)
                    )
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = currencySymbol,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = scaledSp(16, s),
                        fontWeight = FontWeight.Medium,
                        lineHeight = scaledSp(18, s)
                    )

                    Spacer(modifier = Modifier.width(scaled(6, s)))

                    Text(
                        text = formatter.format(displayBalance),
                        color = Color.White,
                        fontSize = scaledSp(28, s),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = scaledSp(1, s),
                        lineHeight = scaledSp(32, s)
                    )
                }
            }

            // --- Bottom row: tipo + últimos 4 dígitos ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
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
                    fontSize = scaledSp(14, s),
                    fontWeight = FontWeight.Medium,
                    lineHeight = scaledSp(16, s),
                    modifier = Modifier.padding(bottom = scaled(4, s))
                )

                if (account.lastFourDigits != null) {
                    Text(
                        text = "**** ${account.lastFourDigits}",
                        color = Color.White,
                        fontSize = scaledSp(18, s),
                        fontWeight = FontWeight.Normal,
                        letterSpacing = scaledSp(2, s),
                        lineHeight = scaledSp(20, s)
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────
// Helpers de escalado
// ──────────────────────────────────────────────

/** Convierte un valor base (dp) aplicando el factor de escala. */
private fun scaled(baseDp: Int, scaleFactor: Float): Dp =
    (baseDp * scaleFactor).dp

/** Convierte un valor base (sp) aplicando el factor de escala. */
private fun scaledSp(baseSp: Int, scaleFactor: Float): TextUnit =
    (baseSp * scaleFactor).sp

// ──────────────────────────────────────────────
// Iconos
// ──────────────────────────────────────────────

@Composable
private fun AccountCardIcon(account: Account, scaleFactor: Float) {
    when {
        account.type == AccountType.CASH -> {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = "Efectivo",
                tint = Color.White,
                modifier = Modifier.size(scaled(28, scaleFactor))
            )
        }
        account.cardNetwork != null -> {
            CardNetworkIcon(network = account.cardNetwork, scaleFactor = scaleFactor)
        }
        else -> {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(scaled(28, scaleFactor))
            )
        }
    }
}

@Composable
fun CardNetworkIcon(
    network: CardNetwork,
    modifier: Modifier = Modifier,
    scaleFactor: Float = 1f
) {
    val s = scaleFactor
    when (network) {
        CardNetwork.MASTERCARD -> {
            Box(
                modifier = modifier
                    .width(scaled(42, s))
                    .height(scaled(26, s)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(scaled(26, s))
                        .align(Alignment.CenterStart)
                        .background(Color.White, CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(scaled(26, s))
                        .align(Alignment.CenterEnd)
                        .border(scaled(1, s), Color.White, CircleShape)
                )
            }
        }
        CardNetwork.VISA -> {
            Text(
                text = "VISA",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = scaledSp(20, s),
                lineHeight = scaledSp(22, s),
                modifier = modifier
            )
        }
        CardNetwork.AMEX -> {
            Text(
                text = "AMEX",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = scaledSp(16, s),
                lineHeight = scaledSp(18, s),
                modifier = modifier
            )
        }
        else -> {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = Color.White,
                modifier = modifier.size(scaled(28, s))
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