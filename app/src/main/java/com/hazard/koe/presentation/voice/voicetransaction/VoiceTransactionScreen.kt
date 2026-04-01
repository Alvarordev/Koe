package com.hazard.koe.presentation.voice.voicetransaction

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.koin.androidx.compose.koinViewModel
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun VoiceTransactionScreen(
    onNavigateBack: () -> Unit,
    onTransactionCreated: (VoiceTransactionCreationResult) -> Unit,
    viewModel: VoiceTransactionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            ensureLocationIfEnabled(uiState, context, fusedLocationClient, viewModel::updateLocation)
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

    LaunchedEffect(Unit) {
        viewModel.creationResult.collect { event ->
            onTransactionCreated(event)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    enabled = uiState.phase != VoiceTransactionPhase.PROCESSING
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            CountdownTimer(
                countdownSeconds = uiState.countdownSeconds,
                isRecording = uiState.phase == VoiceTransactionPhase.RECORDING
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatusLabel(phase = uiState.phase)

            Spacer(modifier = Modifier.weight(1f))

            WaveformVisualizer(
                rmsLevel = uiState.rmsLevel,
                isRecording = uiState.phase == VoiceTransactionPhase.RECORDING
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.phase == VoiceTransactionPhase.ERROR && !uiState.errorMessage.isNullOrBlank()) {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            RecordButton(
                phase = uiState.phase,
                isSubmitting = uiState.isSubmitting,
                countdownSeconds = uiState.countdownSeconds,
                onClick = {
                    when (uiState.phase) {
                        VoiceTransactionPhase.IDLE,
                        VoiceTransactionPhase.ERROR -> {
                            if (uiState.isLocationEnabled) {
                                val hasLocationPermission = ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED ||
                                    ActivityCompat.checkSelfPermission(
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

                            val micGranted = ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (micGranted) {
                                viewModel.startRecording()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }

                        VoiceTransactionPhase.RECORDING -> viewModel.stopRecording()
                        VoiceTransactionPhase.PROCESSING -> Unit
                    }
                }
            )

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun CountdownTimer(
    countdownSeconds: Int,
    isRecording: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "countdown_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 0.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "countdown_alpha"
    )

    val displayAlpha = if (isRecording) pulseAlpha else 1f

    Text(
        text = "0:%02d".format(countdownSeconds),
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            fontFeatureSettings = "tnum"
        ),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = displayAlpha)
    )
}

@Composable
private fun StatusLabel(phase: VoiceTransactionPhase) {
    AnimatedContent(
        targetState = phase,
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
        label = "status_label"
    ) { targetPhase ->
        Text(
            text = when (targetPhase) {
                VoiceTransactionPhase.IDLE -> "Toca el micrófono para empezar"
                VoiceTransactionPhase.RECORDING -> "Escuchando..."
                VoiceTransactionPhase.PROCESSING -> "Procesando..."
                VoiceTransactionPhase.ERROR -> "Intenta de nuevo"
            },
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WaveformVisualizer(
    rmsLevel: Float,
    isRecording: Boolean
) {
    val barCount = 35
    val barSeeds = remember { List(barCount) { Random.nextFloat() } }

    val animatedBars = barSeeds.mapIndexed { index, seed ->
        val targetHeight = if (isRecording) {
            val baseLevel = rmsLevel.coerceIn(0f, 1f)
            val variation = sin(seed * Math.PI * 2 + index * 0.5).toFloat() * 0.3f
            (baseLevel * 0.7f + (baseLevel + variation).coerceIn(0.05f, 1f) * 0.3f).coerceIn(0.05f, 1f)
        } else {
            0.03f
        }

        val animatedHeight by animateFloatAsState(
            targetValue = targetHeight,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = 300f
            ),
            label = "bar_$index"
        )
        animatedHeight
    }

    val barOpacities = remember { List(barCount) { 0.4f + Random.nextFloat() * 0.6f } }
    val primaryColor = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current
    val barWidthPx = with(density) { 3.dp.toPx() }
    val gapPx = with(density) { 4.dp.toPx() }

    Canvas(
        modifier = Modifier
            .size(width = 280.dp, height = 120.dp)
    ) {
        val centerY = size.height / 2f
        val totalWidth = barCount * barWidthPx + (barCount - 1) * gapPx
        val startX = (size.width - totalWidth) / 2f

        for (i in 0 until barCount) {
            val x = startX + i * (barWidthPx + gapPx)
            val barHeight = animatedBars[i] * size.height * 0.8f
            val halfHeight = barHeight / 2f
            val opacity = barOpacities[i]

            drawRoundRect(
                color = primaryColor.copy(alpha = opacity),
                topLeft = Offset(x, centerY - halfHeight),
                size = Size(barWidthPx, barHeight.coerceAtLeast(barWidthPx)),
                cornerRadius = CornerRadius(barWidthPx / 2f, barWidthPx / 2f)
            )
        }
    }
}

@Composable
private fun RecordButton(
    phase: VoiceTransactionPhase,
    isSubmitting: Boolean,
    countdownSeconds: Int,
    onClick: () -> Unit
) {
    val isRecording = phase == VoiceTransactionPhase.RECORDING
    val isProcessing = phase == VoiceTransactionPhase.PROCESSING || isSubmitting
    val primaryColor = MaterialTheme.colorScheme.primary

    val progressFraction by animateFloatAsState(
        targetValue = if (isRecording) {
            countdownSeconds.toFloat() / VoiceTransactionUiState.RECORDING_DURATION_SECONDS.toFloat()
        } else {
            1f
        },
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "progress_arc"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_ring")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isRecording) 0.2f else 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(100.dp)
    ) {
        if (isRecording) {
            Canvas(modifier = Modifier.size((80 * pulseScale).dp)) {
                drawCircle(
                    color = primaryColor.copy(alpha = pulseAlpha),
                    radius = size.minDimension / 2f
                )
            }
        }

        if (isRecording) {
            Canvas(modifier = Modifier.size(80.dp)) {
                drawArc(
                    color = primaryColor.copy(alpha = 0.3f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progressFraction,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(80.dp),
                color = primaryColor,
                trackColor = primaryColor.copy(alpha = 0.15f),
                strokeWidth = 3.dp
            )
        }

        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            enabled = !isProcessing
        ) {
            AnimatedContent(
                targetState = isRecording,
                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                label = "mic_stop_icon"
            ) { recording ->
                if (recording) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Detener",
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Grabar",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

private fun ensureLocationIfEnabled(
    state: VoiceTransactionUiState,
    context: android.content.Context,
    client: FusedLocationProviderClient,
    onResult: (Double?, Double?) -> Unit
) {
    if (!state.isLocationEnabled) return
    captureLocationIfPermitted(client, context, onResult)
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
