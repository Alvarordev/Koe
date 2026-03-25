package com.example.tracker.presentation.transfer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.tracker.data.model.Account
import com.example.tracker.presentation.addtransaction.KeyboardKey
import com.example.tracker.presentation.transfer.components.TransferAmountContent

@Composable
fun TransferAmountScreen(
    uiState: TransferUiState,
    onDestinationSelected: (Account) -> Unit,
    onKeyPress: (KeyboardKey) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues
) {
    TransferAmountContent(
        uiState = uiState,
        onDestinationSelected = onDestinationSelected,
        onKeyPress = onKeyPress,
        onDescriptionChange = onDescriptionChange,
        onSubmit = onSubmit,
        onDismiss = onNavigateBack,
        modifier = Modifier.padding(contentPadding)
    )
}
