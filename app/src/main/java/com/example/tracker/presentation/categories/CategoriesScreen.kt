package com.example.tracker.presentation.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracker.data.model.Category
import com.example.tracker.data.model.relations.CategoryIdSummary
import com.example.tracker.data.model.relations.RecurringRuleWithDetails
import com.example.tracker.presentation.categories.components.CategoryGridCard
import com.example.tracker.presentation.categories.components.SubscriptionServiceItem

@Composable
fun CategoriesScreen(
    contentPadding: PaddingValues,
    uiState: CategoriesUiState,
    onCategoryClick: (Long) -> Unit,
    onAddCategoryClick: () -> Unit = {}
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Gastos", "Ingresos", "Suscripciones")
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = statusBarPadding.calculateTopPadding(),
                bottom = statusBarPadding.calculateBottomPadding()
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categorias",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onAddCategoryClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir categoría",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
            tabs.forEachIndexed { index, title ->
                TextButton(onClick = { selectedTabIndex = index }) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selectedTabIndex == index)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (index < tabs.lastIndex) {
                    Spacer(Modifier.width(0.dp))
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Spacer(Modifier.height(12.dp))

        when (selectedTabIndex) {
            0 -> CategoryGridContent(
                categories = uiState.expenseCategories,
                summaries = uiState.categorySummaries,
                onCategoryClick = onCategoryClick,
                bottomPadding = contentPadding
            )
            1 -> CategoryGridContent(
                categories = uiState.incomeCategories,
                summaries = uiState.categorySummaries,
                onCategoryClick = onCategoryClick,
                bottomPadding = contentPadding
            )
            2 -> SubscriptionsContent(
                subscriptionRules = uiState.subscriptionRules
            )
        }
    }
}

@Composable
private fun CategoryGridContent(
    categories: List<Category>,
    summaries: Map<Long, CategoryIdSummary>,
    onCategoryClick: (Long) -> Unit,
    bottomPadding: PaddingValues = PaddingValues()
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = 16.dp + bottomPadding.calculateBottomPadding()
        ),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        items(
            items = categories,
            key = { it.id }
        ) { category ->
            CategoryGridCard(
                category = category,
                summary = summaries[category.id],
                onClick = { onCategoryClick(category.id) }
            )
        }
    }
}

@Composable
private fun SubscriptionsContent(
    subscriptionRules: List<RecurringRuleWithDetails>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = subscriptionRules,
            key = { it.rule.id }
        ) { ruleWithDetails ->
            SubscriptionServiceItem(ruleWithDetails = ruleWithDetails)
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}
