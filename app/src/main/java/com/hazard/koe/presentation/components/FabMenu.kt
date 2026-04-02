package com.hazard.koe.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FabMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    navController: NavController,
    onTransactionPress: () -> Unit,
    onVoiceTransactionPress: () -> Unit,
    extraOffsetY: Dp = 0.dp
) {
    val haptic = LocalHapticFeedback.current

    FloatingActionButtonMenu(
        expanded = expanded,
        modifier = Modifier.offset(x = 12.dp, y = 12.dp + extraOffsetY),
        button = {
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    onExpandedChange(!expanded)
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.background
            ) {
                val rotation by animateFloatAsState(
                    targetValue = if (expanded) 135f else 0f,
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    label = "fab_rotation"
                )
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    modifier = Modifier.graphicsLayer { rotationZ = rotation }
                )
            }
        }
    ) {
        FloatingActionButtonMenuItem(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onExpandedChange(false)
                navController.navigate("transfer_source")
            },
            icon = { Icon(Icons.Default.SwapVert, contentDescription = null) },
            text = { Text("Transferencia") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.background
        )
        FloatingActionButtonMenuItem(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onExpandedChange(false)
                onTransactionPress()
            },
            icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
            text = { Text("Operación") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.background
        )
        FloatingActionButtonMenuItem(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onExpandedChange(false)
                onVoiceTransactionPress()
            },
            icon = { Icon(Icons.Default.Mic, contentDescription = null) },
            text = { Text("Voz") },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.background
        )
    }
}
