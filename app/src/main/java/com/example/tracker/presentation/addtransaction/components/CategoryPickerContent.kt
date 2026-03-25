package com.example.tracker.presentation.addtransaction.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.tracker.data.enums.CategoryType
import com.example.tracker.data.model.Category
import com.example.tracker.data.model.relations.CategorySummary
import com.example.tracker.presentation.addtransaction.AddTransactionUiState
import com.example.tracker.presentation.components.EmojiText
import com.example.tracker.presentation.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerContent(
    uiState: AddTransactionUiState,
    onCategorySelected: (Category) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val activeCategories = uiState.categories.filter { !it.isArchived }
    val tabTitles = listOf("Expenses", "Incomes")
    val currencyCode = uiState.selectedAccount?.currencyCode ?: "USD"

    val filteredCategories = when (selectedTabIndex) {
        0 -> activeCategories.filter { it.type == CategoryType.EXPENSE }
        else -> activeCategories.filter { it.type == CategoryType.INCOME }
    }.sortedBy { it.sortOrder }

    Column {
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.padding(horizontal = 16.dp),
            containerColor = Color.Transparent,
            divider = {}
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredCategories) { category ->
                CategoryListItem(
                    category = category,
                    summary = uiState.categorySummaries[category.id],
                    currencyCode = currencyCode,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCategorySelected(category)
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryListItem(
    category: Category,
    summary: CategorySummary?,
    currencyCode: String,
    onClick: () -> Unit
) {
    val categoryColor = try {
        Color(category.color.toColorInt())
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }
    val count = summary?.count ?: 0
    val total = summary?.total ?: 0L

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(19.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(19.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(47.dp)
                .clip(CircleShape)
                .background(color = categoryColor),
            contentAlignment = Alignment.Center
        ) {
            EmojiText(
                text = category.emoji,
                style = TextStyle(fontSize = 24.sp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$count ${if (count == 1) "Operaci\u00F3n" else "Operaciones"}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = CurrencyFormatter.formatBalance(total, currencyCode),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
