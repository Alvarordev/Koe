package com.hazard.koe.presentation.accounts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hazard.koe.data.model.FormalLoan
import com.hazard.koe.presentation.accounts.CasualLoanSummaryWithPerson
import com.hazard.koe.presentation.util.CurrencyFormatter

@Composable
fun LoansRow(
    casualLoanSummaries: List<CasualLoanSummaryWithPerson>,
    formalLoans: List<FormalLoan>,
    onAddLoanClick: () -> Unit,
    onCasualLoanClick: (Long) -> Unit,
    onFormalLoanClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Préstamos y Deudas",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onAddLoanClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Agregar")
            }
        }

        if (casualLoanSummaries.isEmpty() && formalLoans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay préstamos o deudas activas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(casualLoanSummaries) { summary ->
                    CasualLoanCard(
                        summary = summary,
                        onClick = { onCasualLoanClick(summary.personId) }
                    )
                }
                items(formalLoans) { loan ->
                    FormalLoanCard(
                        loan = loan,
                        onClick = { onFormalLoanClick(loan.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CasualLoanCard(
    summary: CasualLoanSummaryWithPerson,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalAmount = summary.totalLend + summary.totalBorrow
    val isLend = summary.totalLend > summary.totalBorrow
    val displayAmount = if (summary.totalLend > 0 && summary.totalBorrow > 0) {
        "Me deben: ${CurrencyFormatter.formatBalance(summary.totalLend, "USD")}\nDebo: ${CurrencyFormatter.formatBalance(summary.totalBorrow, "USD")}"
    } else if (summary.totalLend > 0) {
        CurrencyFormatter.formatBalance(summary.totalLend, "USD")
    } else {
        CurrencyFormatter.formatBalance(summary.totalBorrow, "USD")
    }

    Box(
        modifier = modifier
            .width(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isLend) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = summary.personEmoji ?: "👤",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = summary.personName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (summary.totalLend > 0 && summary.totalBorrow > 0) {
                    "Varios"
                } else if (isLend) {
                    "Me prestó"
                } else {
                    "Debo"
                },
                fontSize = 12.sp,
                color = if (isLend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = displayAmount,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun FormalLoanCard(
    loan: FormalLoan,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏦",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = loan.lenderName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = loan.name,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = CurrencyFormatter.formatBalance(loan.outstandingBalance, loan.currencyCode),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${loan.termMonths} meses",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}