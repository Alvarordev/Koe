package com.example.tracker.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tracker.presentation.components.DaySeparator
import com.example.tracker.presentation.components.TransactionRow
import com.example.tracker.presentation.home.components.BalanceSummaryCard
import com.example.tracker.presentation.home.components.DateFilterDialog
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    if (uiState.showDateFilterDialog) {
        DateFilterDialog(
            currentMode = uiState.dateFilterMode,
            onModeSelected = viewModel::onDateFilterSelected,
            onDismiss = viewModel::onDismissDateFilterDialog
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = statusBarPadding.calculateTopPadding())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(
            onClick = viewModel::onToggleDateFilterDialog,
            modifier = Modifier.heightIn(min = 24.dp)
        ) {
            Text(
                text = uiState.dateFilterMode.label(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(12.dp))

        BalanceSummaryCard(
            expense = uiState.expense,
            income = uiState.income,
            totalAccountBalance = uiState.totalAccountBalance
        )

        // LazyColumn dentro de un Box para el overlay de fade
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 2.dp),
                contentPadding = PaddingValues(
                    top = 10.dp, // el espacio donde ocurre el fade
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

            // Overlay gradiente: simula blur/fade de los items al acercarse al card
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
}
