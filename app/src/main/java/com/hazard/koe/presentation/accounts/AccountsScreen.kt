package com.hazard.koe.presentation.accounts

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hazard.koe.R
import com.hazard.koe.presentation.accounts.components.AccountsCarousel
import com.hazard.koe.presentation.accounts.components.UpcomingPaymentsCard
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
            .verticalScroll(rememberScrollState())
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
                    painter = painterResource(R.drawable.plus),
                    modifier = Modifier
                        .size(26.dp),
                    contentDescription = "Agregar cuenta"
                )
            }
        }

        AccountsCarousel(
            accounts = uiState.accounts,
            onAccountClick = onAccountClick,
        )

        if (uiState.upcomingItems.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Proximos Pagos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(14.dp))

            UpcomingPaymentsCard(
                items = uiState.upcomingItems,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
