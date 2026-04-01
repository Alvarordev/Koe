package com.hazard.koe.presentation.categories.addcategory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hazard.koe.data.enums.CategoryType
import com.hazard.koe.presentation.categories.components.EmojiPickerDialog
import com.hazard.koe.presentation.components.EmojiText
import com.hazard.koe.presentation.components.SegmentedControl
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID
import androidx.core.graphics.toColorInt

private val categoryColorPalette = listOf(
    "#4679FB", "#FA0D5E", "#FB6A3C", "#04C454",
    "#6446FB", "#FB4141", "#FBC728", "#37FBFB", "#A9FB37"
)

private fun parseColor(hex: String): Color = runCatching {
    Color(hex.toColorInt())
}.getOrDefault(Color(0xFF1A73E8))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategorySheet(
    categoryId: Long?,
    onDismiss: () -> Unit
) {
    val viewModelKey = remember { UUID.randomUUID().toString() }
    val viewModel: AddEditCategoryViewModel = koinViewModel(
        key = viewModelKey,
        parameters = { parametersOf(categoryId) }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showEmojiPicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            viewModel.resetSubmitSuccess()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        contentWindowInsets = { WindowInsets.navigationBars }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                }
                TextButton(
                    onClick = viewModel::submit,
                    enabled = uiState.name.isNotBlank()
                ) {
                    Text(
                        text = "Guardar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (uiState.name.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(parseColor(uiState.color))
                        .clickable { showEmojiPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    EmojiText(
                        text = uiState.emoji,
                        style = TextStyle(fontSize = 32.sp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = 2.dp, y = 2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Nombre de la categoría",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                BasicTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            if (uiState.name.isEmpty()) {
                                Text(
                                    text = "Ej. Entretenimiento",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tipo de categoría",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                SegmentedControl(
                    items = listOf("Gastos", "Ingresos"),
                    selectedIndex = if (uiState.categoryType == CategoryType.EXPENSE) 0 else 1,
                    onItemSelected = { index ->
                        viewModel.updateType(
                            if (index == 0) CategoryType.EXPENSE else CategoryType.INCOME
                        )
                    },
                    activeColor = MaterialTheme.colorScheme.surface,
                    activeTextColor = MaterialTheme.colorScheme.onBackground,
                    inactiveColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    inactiveTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Selecciona un color",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    overscrollEffect = null
                ) {
                    items(categoryColorPalette) { hex ->
                        val isSelected = uiState.color == hex
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(parseColor(hex))
                                .then(
                                    if (isSelected) Modifier.border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        shape = CircleShape
                                    ) else Modifier
                                )
                                .clickable { viewModel.updateColor(hex) }
                        )
                    }
                }
            }
        }
    }

    if (showEmojiPicker) {
        EmojiPickerDialog(
            onEmojiSelected = { emoji ->
                viewModel.updateEmoji(emoji)
                showEmojiPicker = false
            },
            onDismiss = { showEmojiPicker = false }
        )
    }
}
