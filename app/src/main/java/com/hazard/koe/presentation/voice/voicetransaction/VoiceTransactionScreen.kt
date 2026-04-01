package com.hazard.koe.presentation.voice.voicetransaction

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.hazard.koe.presentation.util.CurrencyFormatter
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.koin.androidx.compose.koinViewModel

@Composable
fun VoiceTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: VoiceTransactionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startRecording()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            captureLocationIfPermitted(fusedLocationClient, context) { lat, lng ->
                viewModel.updateLocation(lat, lng)
            }
        }
    }

    LaunchedEffect(uiState.phase) {
        if (uiState.phase == VoiceTransactionPhase.CONFIRM && uiState.isLocationEnabled) {
            val hasLocationPermission = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (hasLocationPermission) {
                captureLocationIfPermitted(fusedLocationClient, context) { lat, lng ->
                    viewModel.updateLocation(lat, lng)
                }
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.consumeSaveSuccess()
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Transacción por voz",
            style = MaterialTheme.typography.headlineSmall
        )

        when (uiState.phase) {
            VoiceTransactionPhase.IDLE -> {
                VoicePrimaryAction(
                    text = "Iniciar grabación",
                    icon = Icons.Default.Mic,
                    onClick = {
                        val granted = ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            viewModel.startRecording()
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                )
            }

            VoiceTransactionPhase.RECORDING -> {
                Text("Grabando…")
                OutlinedButton(onClick = { viewModel.stopRecording() }) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Detener")
                }
            }

            VoiceTransactionPhase.PROCESSING -> {
                Text("Procesando audio…")
            }

            VoiceTransactionPhase.CONFIRM -> {
                ConfirmationCard(
                    uiState = uiState,
                    onConfirm = viewModel::confirmAndSave,
                    onManualEdit = viewModel::goManualEdit
                )
            }

            VoiceTransactionPhase.MANUAL_EDIT -> {
                ManualEditSection(
                    uiState = uiState,
                    onAmountChange = viewModel::updateAmountFromInput,
                    onDescriptionChange = viewModel::updateDescription,
                    onSave = viewModel::confirmAndSave
                )
            }

            VoiceTransactionPhase.SUCCESS -> {
                Text("Transacción guardada")
                Button(onClick = onNavigateBack) {
                    Text("Volver")
                }
            }

            VoiceTransactionPhase.ERROR -> {
                Text(
                    text = uiState.errorMessage ?: "Ocurrió un error",
                    color = MaterialTheme.colorScheme.error
                )
                RowActions(
                    onPrimary = viewModel::retry,
                    primaryLabel = "Reintentar",
                    onSecondary = viewModel::goManualEdit,
                    secondaryLabel = "Editar manual"
                )
            }
        }
    }
}

@Composable
private fun VoicePrimaryAction(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(onClick = onClick) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
private fun ConfirmationCard(
    uiState: VoiceTransactionUiState,
    onConfirm: () -> Unit,
    onManualEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Confianza: ${(uiState.confidence * 100).toInt()}%")
            Text("Cuenta: ${uiState.selectedAccount?.name ?: "Sin seleccionar"}")
            Text("Categoría: ${uiState.selectedCategory?.name ?: "Sin seleccionar"}")
            Text("Monto: ${CurrencyFormatter.formatBalance(uiState.amountMinor, uiState.selectedAccount?.currencyCode ?: "USD")}")
            if (uiState.description.isNotBlank()) {
                Text("Detalle: ${uiState.description}")
            }
            if (uiState.isLocationEnabled) {
                val locationText = if (uiState.latitude != null && uiState.longitude != null) {
                    "Ubicación lista"
                } else {
                    "Ubicación pendiente"
                }
                Text(locationText)
            }
            RowActions(
                onPrimary = onConfirm,
                primaryLabel = "Confirmar y guardar",
                onSecondary = onManualEdit,
                secondaryLabel = "Editar"
            )
        }
    }
}

@Composable
private fun ManualEditSection(
    uiState: VoiceTransactionUiState,
    onAmountChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit
) {
    var amountInput by remember(uiState.amountMinor) {
        mutableStateOf((uiState.amountMinor / 100.0).toString())
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = amountInput,
            onValueChange = {
                amountInput = it
                onAmountChange(it)
            },
            label = { Text("Monto") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            label = { Text("Detalle") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text("Guardar")
        }
    }
}

@Composable
private fun RowActions(
    onPrimary: () -> Unit,
    primaryLabel: String,
    onSecondary: () -> Unit,
    secondaryLabel: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onPrimary, modifier = Modifier.fillMaxWidth()) {
            Text(primaryLabel)
        }
        OutlinedButton(onClick = onSecondary, modifier = Modifier.fillMaxWidth()) {
            Text(secondaryLabel)
        }
    }
}

@SuppressLint("MissingPermission")
private fun captureLocationIfPermitted(
    client: FusedLocationProviderClient,
    context: android.content.Context,
    onResult: (Double?, Double?) -> Unit
) {
    val hasPerm = ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasPerm) {
        onResult(null, null)
        return
    }

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
