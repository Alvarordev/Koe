package com.example.tracker.presentation.addtransaction.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.tracker.data.enums.SupportedCurrency
import com.example.tracker.data.model.Account
import com.example.tracker.presentation.addtransaction.AddTransactionUiState
import com.example.tracker.presentation.addtransaction.KeyboardKey
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@Composable
fun AmountEntryContent(
    uiState: AddTransactionUiState,
    onAccountSelected: (Account) -> Unit,
    onKeyPress: (KeyboardKey) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
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

    val currencySymbol = uiState.selectedAccount?.currencyCode?.let { code ->
        SupportedCurrency.entries.find { it.code == code }?.symbol
    } ?: "$"
    val amountDisplay = "$currencySymbol${uiState.amountString.ifEmpty { "0" }}"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        AccountSelectorRow(
            uiState = uiState,
            onAccountSelected = onAccountSelected
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = amountDisplay,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
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
        Spacer(modifier = Modifier.height(8.dp))
        TransactionKeyboard(
            onKey = onKeyPress,
            onSubmit = onSubmit,
            onCurrencySelect = {},
            isSubmitting = uiState.isSubmitting,
            isLocationEnabled = isLocationEnabled,
            onLocationToggle = handleLocationToggle,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun AccountSelectorRow(
    uiState: AddTransactionUiState,
    onAccountSelected: (Account) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val activeAccounts = uiState.accounts.filter { !it.isArchived }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        uiState.selectedCategory?.let { category ->
            Text(
                text = "${category.emoji} ${category.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(
                    text = uiState.selectedAccount?.name ?: "No account",
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select account"
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (activeAccounts.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No accounts available") },
                        onClick = { expanded = false }
                    )
                } else {
                    activeAccounts.sortedBy { it.sortOrder }.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(account.name) },
                            onClick = {
                                onAccountSelected(account)
                                expanded = false
                            }
                        )
                    }
                }
            }
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
