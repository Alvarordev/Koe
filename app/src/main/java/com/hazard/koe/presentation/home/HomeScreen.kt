package com.hazard.koe.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hazard.koe.R
import com.hazard.koe.presentation.components.DaySeparator
import com.hazard.koe.presentation.components.AccountFilterPill
import com.hazard.koe.presentation.components.CategoryFilterPill
import com.hazard.koe.presentation.components.TransactionRow
import com.hazard.koe.presentation.components.filterPillEnterTransition
import com.hazard.koe.presentation.components.filterPillExitTransition
import com.hazard.koe.presentation.home.components.BalanceSummaryCard
import com.hazard.koe.presentation.home.components.DateFilterDialog
import com.hazard.koe.presentation.home.components.HomeFilterSheet
import com.hazard.koe.presentation.home.components.TransactionDetailSheet
import com.hazard.koe.data.model.relations.TransactionWithDetails
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onEditTransaction: (Long) -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTransaction by remember { mutableStateOf<TransactionWithDetails?>(null) }
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    if (uiState.showDateFilterDialog) {
        DateFilterDialog(
            currentMode = uiState.dateFilterMode,
            onModeSelected = viewModel::onDateFilterSelected,
            onDismiss = viewModel::onDismissDateFilterDialog
        )
    }

    if (uiState.showFilterSheet) {
        HomeFilterSheet(
            categories = uiState.availableCategories,
            accounts = uiState.availableAccounts,
            selectedCategoryIds = uiState.selectedCategoryIds,
            selectedAccountIds = uiState.selectedAccountIds,
            onToggleCategory = viewModel::onToggleCategoryFilter,
            onToggleAccount = viewModel::onToggleAccountFilter,
            onDismiss = viewModel::onDismissFilterSheet
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = statusBarPadding.calculateTopPadding())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onNavigateToMap,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Map,
                    contentDescription = "Mapa de transacciones",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            TextButton(
                onClick = viewModel::onToggleDateFilterDialog,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = uiState.dateFilterMode.label(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        painter = painterResource(R.drawable.nav_arrow_down),
                        contentDescription = "arrow-down",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            IconButton(
                onClick = viewModel::onToggleFilterSheet,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = "Filtros",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        BalanceSummaryCard(
            expense = uiState.expense,
            income = uiState.income,
            totalAccountBalance = uiState.totalAccountBalance
        )

        AnimatedVisibility(
            visible = uiState.selectedCategoryIds.isNotEmpty() || uiState.selectedAccountIds.isNotEmpty(),
            enter = filterPillEnterTransition(),
            exit = filterPillExitTransition()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.availableCategories.forEach { category ->
                        AnimatedVisibility(
                            visible = category.id in uiState.selectedCategoryIds,
                            enter = filterPillEnterTransition(),
                            exit = filterPillExitTransition()
                        ) {
                            CategoryFilterPill(
                                category = category,
                                selected = true,
                                onClick = { viewModel.onRemoveCategoryFilter(category.id) }
                            )
                        }
                    }

                    uiState.availableAccounts.forEach { account ->
                        AnimatedVisibility(
                            visible = account.id in uiState.selectedAccountIds,
                            enter = filterPillEnterTransition(),
                            exit = filterPillExitTransition()
                        ) {
                            AccountFilterPill(
                                account = account,
                                selected = true,
                                onClick = { viewModel.onRemoveAccountFilter(account.id) }
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 2.dp),
                contentPadding = PaddingValues(
                    top = 10.dp,
                    bottom = contentPadding.calculateBottomPadding()
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                uiState.dayGroups.forEach { dayGroup ->
                    item(key = "day-${dayGroup.date}") {
                        DaySeparator(date = dayGroup.date)
                    }

                    items(
                        items = dayGroup.transactions,
                        key = { it.transaction.id }
                    ) { transaction ->
                        TransactionRow(
                            transaction = transaction,
                            onClick = { selectedTransaction = transaction }
                        )
                    }
                }
            }

            val backgroundColor = MaterialTheme.colorScheme.background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .align(Alignment.TopStart)
                    .background(
                        Brush.verticalGradient(
                            0.0f to backgroundColor,
                            1.0f to backgroundColor.copy(alpha = 0f)
                        )
                    )
            )
        }
    }

    selectedTransaction?.let { txn ->
        TransactionDetailSheet(
            transaction = txn,
            onDismiss = { selectedTransaction = null },
            onDelete = { transaction ->
                viewModel.deleteTransaction(transaction)
                selectedTransaction = null
            },
            onEdit = { transactionWithDetails ->
                selectedTransaction = null
                onEditTransaction(transactionWithDetails.transaction.id)
            }
        )
    }
}
