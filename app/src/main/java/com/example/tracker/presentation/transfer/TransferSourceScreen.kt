package com.example.tracker.presentation.transfer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.tracker.data.model.Account
import com.example.tracker.presentation.transfer.components.SourceAccountPickerContent

@Composable
fun TransferSourceScreen(
    uiState: TransferUiState,
    onAccountSelected: (Account) -> Unit,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues
) {
    SourceAccountPickerContent(
        accounts = uiState.accounts,
        onSourceSelected = onAccountSelected,
        modifier = Modifier.padding(contentPadding)
    )
}
