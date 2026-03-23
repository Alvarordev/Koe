package com.example.tracker.presentation.home

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
import com.example.tracker.presentation.components.DaySeparator
import com.example.tracker.presentation.components.TransactionRow
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = contentPadding
    ) {
        uiState.dayGroups.forEach { dayGroup ->
            item(key = "day-${dayGroup.date}") {
                DaySeparator(date = dayGroup.date)
            }

            items(
                items = dayGroup.transactions,
                key = { it.transaction.id }
            ) { transaction ->
                TransactionRow(transaction = transaction)
                if (transaction != dayGroup.transactions.last()) {
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
