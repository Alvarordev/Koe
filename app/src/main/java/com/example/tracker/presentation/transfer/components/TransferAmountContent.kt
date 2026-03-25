package com.example.tracker.presentation.transfer.components

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.tracker.data.enums.SupportedCurrency
import com.example.tracker.data.model.Account
import com.example.tracker.presentation.addtransaction.KeyboardKey
import com.example.tracker.presentation.addtransaction.components.TransactionKeyboard
import com.example.tracker.presentation.components.AnimatedAmountText
import com.example.tracker.presentation.transfer.TransferUiState
import com.example.tracker.presentation.util.CurrencyFormatter

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TransferAmountContent(
    uiState: TransferUiState,
    onDestinationSelected: (Account) -> Unit,
    onKeyPress: (KeyboardKey) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sourceAccount = uiState.sourceAccount ?: return
    val view = LocalView.current

    val currencySymbol = sourceAccount.currencyCode.let { code ->
        SupportedCurrency.entries.find { it.code == code }?.symbol
    } ?: "$"
    val amountDisplay = uiState.amountString.ifEmpty { "0" }

    var showDestinationPicker by remember { mutableStateOf(false) }

    val conversionText = if (uiState.isCrossCurrency && uiState.destinationAccount != null) {
        val destSymbol = SupportedCurrency.entries.find {
            it.code == uiState.destinationAccount.currencyCode
        }?.symbol ?: "$"
        val sourceAmount = uiState.amountString.toDoubleOrNull() ?: 0.0
        val convertedDisplay = String.format("%.2f", sourceAmount * uiState.exchangeRate)

        "$destSymbol $convertedDisplay (1 ${sourceAccount.currencyCode} = ${String.format("%.4f", uiState.exchangeRate)} ${uiState.destinationAccount.currencyCode})"
    } else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transfer",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AccountCard(
                account = sourceAccount,
                label = "From",
                currencySymbol = currencySymbol,
                amountDisplay = amountDisplay,
                conversionText = conversionText,
                errorMessage = uiState.submitError,
                modifier = Modifier.weight(1f),
                isExpanded = true
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "To",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (uiState.destinationAccount != null) {
                AccountCard(
                    account = uiState.destinationAccount,
                    label = "To",
                    onClick = { showDestinationPicker = true },
                    currencySymbol = currencySymbol,
                    amountDisplay = amountDisplay,
                    modifier = Modifier,
                    isExpanded = false
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(13.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(13.dp))
                        .clickable {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            showDestinationPicker = true
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF363336)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Recibir",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Escoge una cuenta",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TransactionKeyboard(
            onKey = onKeyPress,
            onSubmit = onSubmit,
            onCurrencySelect = {},
            isSubmitting = uiState.isSubmitting,
            isLocationEnabled = false,
            onLocationToggle = {},
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showDestinationPicker) {
        DestinationAccountPicker(
            accounts = uiState.accounts.filter { it.id != uiState.sourceAccount.id },
            onAccountSelected = {
                onDestinationSelected(it)
                showDestinationPicker = false
            },
            onDismiss = { showDestinationPicker = false }
        )
    }
}

@Composable
private fun AccountCard(
    account: Account,
    label: String,
    isExpanded: Boolean,
    onClick: (() -> Unit)? = null,
    currencySymbol: String,
    amountDisplay: String,
    conversionText: String ?= null,
    errorMessage: String ?= null,
    modifier: Modifier
) {
    val accountColor = try {
        Color(account.color.toColorInt())
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(13.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp)

    ) {
        Column(
            modifier = if (isExpanded) Modifier.fillMaxSize() else Modifier
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.heightIn(min = 46.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(accountColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = account.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = CurrencyFormatter.formatBalance(
                        account.currentBalance,
                        account.currencyCode
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isExpanded) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currencySymbol,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .padding(end = 4.dp)
                        )
                        AnimatedAmountText(
                            text = amountDisplay,
                            maxFontSize = 80.sp,
                            minFontSize = 42.sp,
                            shrinkThreshold = 5,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            animationDurationMs = 120,
                        )
                    }

                    if (conversionText != null) {
                        Text(
                            text = conversionText,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }



        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun DestinationAccountPicker(
    accounts: List<Account>,
    onAccountSelected: (Account) -> Unit,
    onDismiss: () -> Unit
) {
    SharedTransitionLayout {
        AnimatedContent(
            targetState = true,
            label = "DestinationPicker",
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(350.dp)
                        .height(400.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(enabled = false) { }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Select Account",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(accounts) { account ->
                                val color = try {
                                    Color(account.color.toColorInt())
                                } catch (_: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onAccountSelected(account) }
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 32.dp, height = 20.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = account.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = CurrencyFormatter.formatBalance(account.currentBalance, account.currencyCode),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
