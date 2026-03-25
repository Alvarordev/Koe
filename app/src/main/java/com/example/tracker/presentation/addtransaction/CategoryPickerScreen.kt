package com.example.tracker.presentation.addtransaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import com.example.tracker.data.model.Category
import com.example.tracker.presentation.addtransaction.components.CategoryPickerContent

@Composable
fun CategoryPickerScreen(
    uiState: AddTransactionUiState,
    onCategorySelected: (Category) -> Unit,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onNavigateBack) {
                Text(
                    text = "Volver",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Agregar operaci\u00F3n",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = {}, enabled = false) {
                Text(
                    text = "Volver",
                    color = androidx.compose.ui.graphics.Color.Transparent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        CategoryPickerContent(
            uiState = uiState,
            onCategorySelected = onCategorySelected
        )
    }
}
