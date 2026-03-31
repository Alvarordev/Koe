package com.example.tracker.presentation.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracker.data.enums.TransactionType
import com.example.tracker.data.model.relations.TransactionWithDetails
import com.example.tracker.ui.theme.ExpenseRed
import com.example.tracker.ui.theme.IncomeGreen
import com.example.tracker.presentation.util.CurrencyFormatter
import androidx.core.graphics.toColorInt

@Composable
fun TransactionRow(
    transaction: TransactionWithDetails,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val category = transaction.category
    val account = transaction.account
    val txn = transaction.transaction

    val categoryColor = try {
        Color(category.color.toColorInt())
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val amountColor = when (txn.type) {
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.INCOME -> IncomeGreen
        else -> MaterialTheme.colorScheme.onSurface
    }

    val rowModifier = modifier
        .fillMaxWidth()
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .padding(vertical = 12.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(categoryColor),
            contentAlignment = Alignment.Center
        ) {
            EmojiText(text = category.emoji, style = TextStyle(fontSize = 20.sp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = txn.description ?: category.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${account.name} \u00B7 ${CurrencyFormatter.formatTime(txn.date)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        Text(
            text = CurrencyFormatter.formatAmount(txn.amount, account.currencyCode, txn.type),
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 12.sp,
            color = amountColor
        )
    }
}
