package com.example.tracker.presentation.accounts.accountdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tracker.data.enums.AccountType
import com.example.tracker.presentation.accounts.accountdetail.components.BalanceChart
import com.example.tracker.presentation.accounts.components.AccountCard
import com.example.tracker.presentation.components.DaySeparator
import com.example.tracker.presentation.components.TransactionRow
import com.example.tracker.presentation.util.CurrencyFormatter
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.Instant
import java.time.ZoneId

private const val FULL_CARD_WIDTH = 340f
private const val THUMBNAIL_WIDTH = 70f

@Composable
fun AccountDetailScreen(
    accountId: Long,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: AccountDetailViewModel = koinViewModel { parametersOf(accountId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val account = uiState.account
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()
    val topScaffoldPadding = contentPadding.calculateTopPadding()
    val topInsetPadding = maxOf(statusBarPadding, topScaffoldPadding)
    val bottomInsetPadding = maxOf(
        contentPadding.calculateBottomPadding(),
        navigationBarPadding.calculateBottomPadding()
    )

    LaunchedEffect(uiState.isArchived) {
        if (uiState.isArchived) onNavigateBack()
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = { Text("Eliminar cuenta") },
            text = {
                Text("Se eliminará esta cuenta y todas sus transacciones asociadas. Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(onClick = viewModel::archiveAccount) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) {
                    Text("Cancelar")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = topInsetPadding,
            bottom = bottomInsetPadding + 16.dp
        )
    ) {
        item {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }
        }

        if (account != null) {
            val displayBalance = when (account.type) {
                AccountType.CREDIT -> {
                    val available = (account.creditLimit ?: 0L) - (account.creditUsed ?: 0L)
                    available
                }
                else -> account.currentBalance
            }

            val balanceLabel = if (account.type == AccountType.CREDIT) "Disponible" else null

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AccountCard(
                        account = account,
                        cardHeight = 50.dp,
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = account.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (balanceLabel != null) {
                            Text(
                                text = balanceLabel,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = CurrencyFormatter.formatBalance(displayBalance, account.currencyCode),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Box {
                        var menuExpanded by remember { mutableStateOf(false) }

                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Opciones"
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar") },
                                onClick = { menuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Eliminar",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.showDeleteDialog()
                                }
                            )
                        }
                    }
                }
            }

            if (account.type != AccountType.CREDIT && uiState.balanceHistory.size >= 2) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        BalanceChart(
                            history = uiState.balanceHistory,
                            currencyCode = account.currencyCode,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            val groupedTransactions = uiState.transactions
                .sortedByDescending { it.transaction.date }
                .groupBy { txn ->
                    Instant.ofEpochMilli(txn.transaction.date)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }

            if (groupedTransactions.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 24.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Movimientos",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        TextButton(onClick = { }) {
                            Text("Ver todos")
                        }
                    }
                }

                groupedTransactions.forEach { (date, transactions) ->
                    item(key = "date_$date") {
                        DaySeparator(
                            date = date,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    items(
                        items = transactions,
                        key = { it.transaction.id }
                    ) { transaction ->
                        TransactionRow(
                            transaction = transaction,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(start = 74.dp)
                        )
                    }
                }
            } else {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sin movimientos",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Las transacciones de esta cuenta aparecerán aquí",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
