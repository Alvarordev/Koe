package com.example.tracker.presentation.addtransaction.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.tracker.data.enums.CategoryType
import com.example.tracker.data.model.Category
import com.example.tracker.presentation.addtransaction.AddTransactionUiState

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

    val filteredCategories = when (selectedTabIndex) {
        0 -> activeCategories.filter { it.type == CategoryType.EXPENSE }
        else -> activeCategories.filter { it.type == CategoryType.INCOME }
    }.sortedBy { it.sortOrder }

    Column {
        SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title) }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredCategories) { category ->
                CategoryGridItem(
                    category = category,
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
private fun CategoryGridItem(
    category: Category,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            text = category.emoji,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
