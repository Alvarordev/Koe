package com.example.tracker.presentation.accounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tracker.presentation.accounts.components.AccountsCarousel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountsScreen(
    onAccountClick: (Long) -> Unit,
    onAddAccountClick: () -> Unit = {},
    viewModel: AccountsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = statusBarPadding.calculateTopPadding())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mis Cuentas",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(onClick = onAddAccountClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar cuenta"
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        AccountsCarousel(
            accounts = uiState.accounts,
            onAccountClick = onAccountClick,
        )
    }
}
