package com.example.tracker.presentation.subscriptions.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import java.time.LocalDate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.tracker.data.enums.SupportedCurrency
import com.example.tracker.data.model.Account
import com.example.tracker.presentation.accounts.components.AccountCard
import com.example.tracker.presentation.categories.components.EmojiPickerDialog
import com.example.tracker.presentation.components.AccountPickerSheet
import com.example.tracker.presentation.components.EmojiText
import com.example.tracker.presentation.subscriptions.SubscriptionIconCatalog
import com.example.tracker.presentation.util.CurrencyFormatter

@Composable
fun SubscriptionDetailScreen(
    uiState: SubscriptionDetailUiState,
    onAmountChange: (String) -> Unit,
    onBillingDayChange: (Int) -> Unit,
    onSelectAccount: (Account) -> Unit,
    onCustomNameChange: (String) -> Unit,
    onEmojiChange: (String) -> Unit,
    onBillCurrentMonthChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onDelete: () -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

    var showEmojiPicker by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }

    val currencySymbol = uiState.selectedAccount?.currencyCode?.let { code ->
        SupportedCurrency.entries.find { it.code == code }?.symbol
    } ?: "S/"

    val isSaveEnabled = uiState.amountString.isNotBlank() &&
        (uiState.amountString.toDoubleOrNull() ?: 0.0) > 0.0 &&
        uiState.selectedAccount != null &&
        uiState.customName.isNotBlank()

    val screenTitle = when {
        uiState.editingId != null -> "Editar suscripción"
        uiState.isCustom -> "Nueva suscripción"
        else -> uiState.service?.name ?: "Suscripción"
    }

    val iconSpec = uiState.iconResName?.let { SubscriptionIconCatalog.forName(it) }

    val selectedAccount = uiState.selectedAccount
    val accountColor = try {
        Color(selectedAccount!!.color.toColorInt())
    } catch (_: Exception) {
        MaterialTheme.colorScheme.outlineVariant
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = statusBarPadding.calculateTopPadding(),
                bottom = navBarPadding.calculateBottomPadding()
            )
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onNavigateBack) {
                Text(
                    text = "Volver",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = screenTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(64.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .then(
                        if (uiState.isCustom) Modifier.clickable { showEmojiPicker = true } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!uiState.isCustom && iconSpec != null) {
                    Icon(
                        painter = painterResource(id = iconSpec.iconRes),
                        contentDescription = uiState.service?.name,
                        modifier = Modifier.size(32.dp),
                        tint = iconSpec.tint ?: Color.Unspecified
                    )
                } else if (uiState.isCustom) {
                    EmojiText(
                        text = uiState.customEmoji,
                        style = TextStyle(fontSize = 28.sp)
                    )
                } else {
                    EmojiText(
                        text = "⭐",
                        style = TextStyle(fontSize = 28.sp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        BasicTextField(
            value = uiState.customName,
            onValueChange = onCustomNameChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(13.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (uiState.customName.isEmpty()) {
                        Text(
                            text = "Nombre de la suscripción",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Se debitará automáticamente de esta cuenta",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(13.dp))
                    .clickable { showAccountSheet = true }
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {

                    if (selectedAccount != null) {
                        AccountCard(
                            account = selectedAccount,
                            cardHeight = 20.dp
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(30.dp)
                                .background(accountColor)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = selectedAccount?.name ?: "Seleccionar cuenta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (selectedAccount != null) {
                        Text(
                            text = CurrencyFormatter.formatBalance(
                                selectedAccount.currentBalance,
                                selectedAccount.currencyCode
                            ),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(13.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currencySymbol,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(4.dp))

            BasicTextField(
                value = uiState.amountString,
                onValueChange = onAmountChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box {
                        if (uiState.amountString.isEmpty()) {
                            Text(
                                text = "0.00",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(13.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Día de cobro",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            BasicTextField(
                value = if (uiState.billingDay == 0) "" else uiState.billingDay.toString(),
                onValueChange = { raw ->
                    val day = raw.filter { it.isDigit() }.take(2).toIntOrNull() ?: 0
                    onBillingDayChange(day.coerceIn(0, 31))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.width(48.dp),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterEnd) {
                        if (uiState.billingDay == 0) {
                            Text(
                                text = "1",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        val today = LocalDate.now()
        val billingDayPassedThisMonth = uiState.billingDay in 1..today.dayOfMonth
        val showBillCurrentMonthToggle = uiState.editingId == null && billingDayPassedThisMonth

        if (showBillCurrentMonthToggle) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(13.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cobrar este mes también",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = uiState.billCurrentMonth,
                    onCheckedChange = onBillCurrentMonthChange
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (uiState.submitError != null) {
            Text(
                text = uiState.submitError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = onSubmit,
            enabled = isSaveEnabled && !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = "Guardar", fontWeight = FontWeight.SemiBold)
        }

        if (uiState.editingId != null) {
            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Eliminar suscripción",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showAccountSheet) {
        AccountPickerSheet(
            accounts = uiState.accounts.filter { !it.isArchived },
            onAccountSelected = { account ->
                onSelectAccount(account)
                showAccountSheet = false
            },
            onDismiss = { showAccountSheet = false }
        )
    }

    if (showEmojiPicker) {
        EmojiPickerDialog(
            onEmojiSelected = { emoji ->
                onEmojiChange(emoji)
                showEmojiPicker = false
            },
            onDismiss = { showEmojiPicker = false }
        )
    }
}
