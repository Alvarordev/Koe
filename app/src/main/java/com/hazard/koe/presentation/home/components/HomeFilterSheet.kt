package com.hazard.koe.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.Category
import com.hazard.koe.presentation.components.AccountFilterPill
import com.hazard.koe.presentation.components.CategoryFilterPill
import com.hazard.koe.presentation.components.filterPillEnterTransition
import com.hazard.koe.presentation.components.filterPillExitTransition

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeFilterSheet(
    categories: List<Category>,
    accounts: List<Account>,
    selectedCategoryIds: Set<Long>,
    selectedAccountIds: Set<Long>,
    onToggleCategory: (Long) -> Unit,
    onToggleAccount: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 4.dp, bottom = 20.dp)
        ) {
            Text(
                text = "Filtros",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            AnimatedVisibility(
                visible = selectedCategoryIds.isNotEmpty() || selectedAccountIds.isNotEmpty(),
                enter = filterPillEnterTransition(),
                exit = filterPillExitTransition()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Seleccionados",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            AnimatedVisibility(
                                visible = category.id in selectedCategoryIds,
                                enter = filterPillEnterTransition(),
                                exit = filterPillExitTransition()
                            ) {
                                CategoryFilterPill(
                                    category = category,
                                    selected = true,
                                    onClick = { onToggleCategory(category.id) }
                                )
                            }
                        }
                        accounts.forEach { account ->
                            AnimatedVisibility(
                                visible = account.id in selectedAccountIds,
                                enter = filterPillEnterTransition(),
                                exit = filterPillExitTransition()
                            ) {
                                AccountFilterPill(
                                    account = account,
                                    selected = true,
                                    onClick = { onToggleAccount(account.id) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Categorías",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    CategoryFilterPill(
                        category = category,
                        selected = category.id in selectedCategoryIds,
                        onClick = { onToggleCategory(category.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Cuentas",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                accounts.forEach { account ->
                    AccountFilterPill(
                        account = account,
                        selected = account.id in selectedAccountIds,
                        onClick = { onToggleAccount(account.id) }
                    )
                }
            }
        }
    }
}
