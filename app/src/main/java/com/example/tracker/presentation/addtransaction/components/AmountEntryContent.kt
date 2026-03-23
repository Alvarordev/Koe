package com.example.tracker.presentation.addtransaction.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.graphics.toColorInt
import com.example.tracker.data.enums.SupportedCurrency
import com.example.tracker.data.model.Account
import com.example.tracker.presentation.addtransaction.AddTransactionUiState
import com.example.tracker.presentation.addtransaction.KeyboardKey
import com.example.tracker.presentation.components.EmojiText
import com.example.tracker.presentation.util.CurrencyFormatter
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountEntryContent(
    uiState: AddTransactionUiState,
    onAccountSelected: (Account) -> Unit,
    onKeyPress: (KeyboardKey) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClearCategory: () -> Unit,
    onDateSelected: (Long) -> Unit,
    isLocationEnabled: Boolean,
    onLocationToggle: (Boolean, Double?, Double?) -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            captureLocation(fusedLocationClient) { lat, lng -> onLocationToggle(true, lat, lng) }
        }
    }
    val handleLocationToggle: () -> Unit = {
        if (isLocationEnabled) {
            onLocationToggle(false, null, null)
        } else {
            val hasPerm = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) {
                captureLocation(fusedLocationClient) { lat, lng -> onLocationToggle(true, lat, lng) }
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    LaunchedEffect(isLocationEnabled) {
        if (isLocationEnabled && uiState.latitude == null) {
            val hasPerm = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) {
                captureLocation(fusedLocationClient) { lat, lng -> onLocationToggle(true, lat, lng) }
            }
        }
    }

    val timeZone = ZoneId.systemDefault()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDateMillis by remember { mutableLongStateOf(uiState.selectedDate) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.selectedDate)
    val initialDateTime = remember(uiState.selectedDate) {
        Instant.ofEpochMilli(uiState.selectedDate).atZone(timeZone)
    }
    val timePickerState = rememberTimePickerState(
        initialHour = initialDateTime.hour,
        initialMinute = initialDateTime.minute
    )

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.forLanguageTag("es")) }
    val displayDate = remember(uiState.selectedDate) {
        val zdt = Instant.ofEpochMilli(uiState.selectedDate).atZone(timeZone)
        dateFormatter.format(zdt).replaceFirstChar { it.uppercase() }
    }

    val currencySymbol = uiState.selectedAccount?.currencyCode?.let { code ->
        SupportedCurrency.entries.find { it.code == code }?.symbol
    } ?: "$"
    val amountDisplay = uiState.amountString.ifEmpty { "0" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showDatePicker = true }
            ) {
                Text(
                    text = displayDate,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(8.dp))

            AccountSelector(
                uiState = uiState,
                onAccountSelected = onAccountSelected
            )
            Spacer(modifier = Modifier.height(56.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = currencySymbol,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp, end = 4.dp)
                )

                Text(
                    text = amountDisplay,
                    fontSize = 80.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.submitError != null) {
                Text(
                    text = uiState.submitError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Column {
            CategoryCard(
                uiState = uiState,
                onClick = onClearCategory
            )

            Spacer(modifier = Modifier.height(8.dp))

            BasicTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                decorationBox = { innerTextField ->
                    Box {
                        if (uiState.description.isEmpty()) {
                            Text(
                                text = "Description (optional)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            TransactionKeyboard(
                onKey = onKeyPress,
                onSubmit = onSubmit,
                onCurrencySelect = {},
                isSubmitting = uiState.isSubmitting,
                isLocationEnabled = isLocationEnabled,
                onLocationToggle = handleLocationToggle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pendingDateMillis = datePickerState.selectedDateMillis ?: uiState.selectedDate
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seleccionar hora",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )
                TimePicker(state = timePickerState)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancelar")
                    }
                    TextButton(onClick = {
                        val localDate = Instant.ofEpochMilli(pendingDateMillis)
                            .atZone(timeZone)
                            .toLocalDate()
                        val combined = localDate
                            .atTime(timePickerState.hour, timePickerState.minute)
                            .atZone(timeZone)
                            .toInstant()
                            .toEpochMilli()
                        onDateSelected(combined)
                        showTimePicker = false
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    uiState: AddTransactionUiState,
    onClick: () -> Unit
) {
    val category = uiState.selectedCategory ?: return

    val categoryColor = try {
        Color(category.color.toColorInt())
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val currencyCode = uiState.selectedAccount?.currencyCode ?: "USD"
    val summary = uiState.categorySummary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(19.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(19.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(47.dp)
                    .clip(CircleShape)
                    .background(color = categoryColor),
                contentAlignment = Alignment.Center
            ) {
                EmojiText(
                    text = category.emoji,
                    style = TextStyle(fontSize = 24.sp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${summary?.count ?: 0} transacciones",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = CurrencyFormatter.formatBalance(summary?.total ?: 0L, currencyCode),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun AccountSelector(
    uiState: AddTransactionUiState,
    onAccountSelected: (Account) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val activeAccounts = uiState.accounts.filter { !it.isArchived }
    val selectedAccount = uiState.selectedAccount
    val accountColor = try {
        Color(selectedAccount!!.color.toColorInt())
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    SharedTransitionLayout {
        AnimatedContent(
            targetState = isExpanded,
            label = "AccountSelectionTransition",
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) }
        ) { targetExpanded ->
            if (!targetExpanded) {
                Box(
                    modifier = Modifier
                        .sharedElement(
                            rememberSharedContentState(key = "account_box"),
                            animatedVisibilityScope = this@AnimatedContent
                        )
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(13.dp))
                        .clickable { isExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    AccountInfoContent(selectedAccount, accountColor)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { isExpanded = false },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .sharedElement(
                                rememberSharedContentState(key = "account_box"),
                                animatedVisibilityScope = this@AnimatedContent
                            )
                            .width(350.dp)
                            .height(400.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable(enabled = false) { }
                    ) {
                        AccountListDetailed(
                            activeAccounts = activeAccounts,
                            onAccountSelected = {
                                onAccountSelected(it)
                                isExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun AccountInfoContent(account: Account?, color: Color) {
    val currencyCode = account?.currencyCode ?: "USD"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .height(20.dp)
                .width(30.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color = color),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = account?.name ?: "No account",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = account?.let { CurrencyFormatter.formatBalance(it.currentBalance, currencyCode) } ?: "",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AccountListDetailed(
    activeAccounts: List<Account>,
    onAccountSelected: (Account) -> Unit
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
            items(activeAccounts) { account ->
                val color = try {
                    Color(account.color.toColorInt())
                } catch (_: Exception) {
                    MaterialTheme.colorScheme.primary
                }

                AccountItemRow(
                    account = account,
                    accountColor = color,
                    onClick = { onAccountSelected(account) }
                )
            }
        }
    }
}

@Composable
private fun AccountItemRow(
    account: Account,
    accountColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                    .background(accountColor)
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


@SuppressLint("MissingPermission")
private fun captureLocation(client: FusedLocationProviderClient, onResult: (Double?, Double?) -> Unit) {
    client.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                onResult(location.latitude, location.longitude)
            } else {
                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMaxUpdateAgeMillis(30_000L)
                    .build()
                client.getCurrentLocation(request, null)
                    .addOnSuccessListener { loc -> onResult(loc?.latitude, loc?.longitude) }
                    .addOnFailureListener { onResult(null, null) }
            }
        }
        .addOnFailureListener { onResult(null, null) }
}
