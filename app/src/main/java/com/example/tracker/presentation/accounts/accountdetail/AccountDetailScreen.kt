package com.example.tracker.presentation.accounts.accountdetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tracker.presentation.accounts.accountdetail.components.AccountSummaryCard
import com.example.tracker.presentation.components.TransactionRow
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    accountId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AccountDetailViewModel = koinViewModel { parametersOf(accountId) }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val account = uiState.account

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(account?.name ?: "Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (account != null) {
                item {
                    AccountSummaryCard(
                        account = account,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            if (uiState.transactions.isNotEmpty()) {
                item {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(
                    items = uiState.transactions,
                    key = { it.transaction.id }
                ) { transaction ->
                    TransactionRow(transaction = transaction)
                    if (transaction != uiState.transactions.last()) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.padding(start = 68.dp)
                        )
                    }
                }
            }
        }
    }
}
