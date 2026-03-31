package com.example.tracker.presentation.addtransaction

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.tracker.data.model.Category
import com.example.tracker.presentation.addtransaction.components.CategoryPickerSheet
import com.example.tracker.presentation.addtransaction.components.TransactionKeyboard
import com.example.tracker.presentation.components.AccountPickerSheet
import com.example.tracker.presentation.components.AnimatedAmountText
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
fun AmountEntryScreen(
    uiState: AddTransactionUiState,
    onAccountSelected: (Account) -> Unit,
    onKeyPress: (KeyboardKey) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCategorySelected: (Category) -> Unit,
    onDateSelected: (Long) -> Unit,
    onLocationToggle: (Boolean, Double?, Double?) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()

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
        if (uiState.isLocationEnabled) {
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

    LaunchedEffect(uiState.isLocationEnabled) {
        if (uiState.isLocationEnabled && uiState.latitude == null) {
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
    var showCategorySheet by remember { mutableStateOf(false) }
    var showAccountSheet by remember { mutableStateOf(false) }
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
            .padding(
                top = statusBarPadding.calculateTopPadding(),
                bottom = navBarPadding.calculateBottomPadding()
            )
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
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

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onClick = { showDatePicker = true }) {
                    Text(
                        text = displayDate,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))

            AccountSelectorBox(
                uiState = uiState,
                onClick = { showAccountSheet = true }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currencySymbol,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp, end = 4.dp)
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
        }

        Column {
            CategoryCard(
                uiState = uiState,
                onClick = { showCategorySheet = true }
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
                isLocationEnabled = uiState.isLocationEnabled,
                onLocationToggle = handleLocationToggle,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showCategorySheet) {
        CategoryPickerSheet(
            uiState = uiState,
            onCategorySelected = onCategorySelected,
            onDismiss = { showCategorySheet = false }
        )
    }

    if (showAccountSheet) {
        AccountPickerSheet(
            accounts = uiState.accounts.filter { !it.isArchived },
            onAccountSelected = onAccountSelected,
            onDismiss = { showAccountSheet = false }
        )
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = uiState.selectedCategory

    Box(
        modifier = modifier
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
        if (category == null) {
            Text(
                text = "Seleccionar categor\u00EDa",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val categoryColor = try {
                Color(category.color.toColorInt())
            } catch (_: Exception) {
                MaterialTheme.colorScheme.primary
            }
            val currencyCode = uiState.selectedAccount?.currencyCode ?: "USD"
            val summary = uiState.categorySummary

            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${summary?.count ?: 0} ${if (summary?.count == 1) "Operaci\u00F3n" else "Operaciones"}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = CurrencyFormatter.formatBalance(summary?.total ?: 0L, currencyCode),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AccountSelectorBox(
    uiState: AddTransactionUiState,
    onClick: () -> Unit
) {
    val selectedAccount = uiState.selectedAccount
    val accountColor = try {
        Color(selectedAccount!!.color.toColorInt())
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(13.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        AccountInfoContent(selectedAccount, accountColor)
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
