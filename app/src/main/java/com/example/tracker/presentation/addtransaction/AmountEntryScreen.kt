package com.example.tracker.presentation.addtransaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.tracker.data.model.Account
import com.example.tracker.presentation.addtransaction.components.AmountEntryContent

@Composable
fun AmountEntryScreen(
    uiState: AddTransactionUiState,
    onAccountSelected: (Account) -> Unit,
    onKeyPress: (KeyboardKey) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClearCategory: () -> Unit,
    onDateSelected: (Long) -> Unit,
    onLocationToggle: (Boolean, Double?, Double?) -> Unit,
    onNavigateBack: () -> Unit
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = statusBarPadding.calculateTopPadding(),
                bottom = navBarPadding.calculateBottomPadding()
            )
    ) {
        AmountEntryContent(
            uiState = uiState,
            onAccountSelected = onAccountSelected,
            onKeyPress = onKeyPress,
            onDescriptionChange = onDescriptionChange,
            onSubmit = onSubmit,
            onClearCategory = onClearCategory,
            onDateSelected = onDateSelected,
            isLocationEnabled = uiState.isLocationEnabled,
            onLocationToggle = onLocationToggle,
            onNavigateBack = onNavigateBack
        )
    }
}
