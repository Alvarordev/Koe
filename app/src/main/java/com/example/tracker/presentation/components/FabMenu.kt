package com.example.tracker.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
    onTransactionPress: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    FloatingActionButtonMenu(
        expanded = expanded,
        modifier = Modifier.offset(x = 12.dp, y = 12.dp),
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
            text = { Text("Trasnferencia") },
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
    }
}
