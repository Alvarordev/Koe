package com.example.tracker.presentation.accounts

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tracker.presentation.accounts.components.AccountCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountsScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onAccountClick: (Long) -> Unit,
    viewModel: AccountsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        items(
            items = uiState.accounts,
            key = { it.id }
        ) { account ->
            AccountCard(
                account = account,
                onClick = { onAccountClick(account.id) }
            )
            if (account != uiState.accounts.last()) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(start = 72.dp)
                )
            }
        }
    }
}
