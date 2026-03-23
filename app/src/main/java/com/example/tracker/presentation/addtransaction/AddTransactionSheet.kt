package com.example.tracker.presentation.addtransaction

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.tracker.data.model.Account
import com.example.tracker.data.model.Category
import com.example.tracker.presentation.addtransaction.components.AmountEntryContent
import com.example.tracker.presentation.addtransaction.components.CategoryPickerContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    uiState: AddTransactionUiState,
    onCategorySelected: (Category) -> Unit,
    onClearCategory: () -> Unit,
    onAccountSelected: (Account) -> Unit,
    onKeyPress: (KeyboardKey) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    onLocationToggle: (Boolean, Double?, Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hasCategory = uiState.selectedCategory != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { WindowInsets.navigationBars },
        modifier = modifier
    ) {
        BackHandler(enabled = hasCategory) {
            onClearCategory()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(bottom = 24.dp)
        ) {
            AnimatedContent(
                targetState = hasCategory,
                transitionSpec = {
                    val spec = spring<IntOffset>(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                    if (targetState) {
                        (slideInHorizontally(spec) { it } + fadeIn())
                            .togetherWith(slideOutHorizontally(spec) { -it } + fadeOut())
                    } else {
                        (slideInHorizontally(spec) { -it } + fadeIn())
                            .togetherWith(slideOutHorizontally(spec) { it } + fadeOut())
                    }
                },
                label = "SheetContentTransition"
            ) { showAmountEntry ->
                if (showAmountEntry) {
                    AmountEntryContent(
                        uiState = uiState,
                        onAccountSelected = onAccountSelected,
                        onKeyPress = onKeyPress,
                        onDescriptionChange = onDescriptionChange,
                        onSubmit = onSubmit,
                        isLocationEnabled = uiState.isLocationEnabled,
                        onLocationToggle = onLocationToggle
                    )
                } else {
                    CategoryPickerContent(
                        uiState = uiState,
                        onCategorySelected = onCategorySelected
                    )
                }
            }
        }
    }
}
