package com.example.tracker.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FabMenu(
    navController: NavController,
    onTransactionPress: () -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    val haptic = LocalHapticFeedback.current

    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    expanded = it
                }
            ) {
                val imageVector by remember {
                    derivedStateOf {
                        if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                    }
                }
                Icon(
                    painter = rememberVectorPainter(imageVector),
                    contentDescription = "Add",
                    modifier = Modifier.animateIcon({ checkedProgress })
                )
            }
        }
    ) {
        FloatingActionButtonMenuItem(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                expanded = false
                navController.navigate("transfer_source")
            },
            icon = { Icon(Icons.Default.SwapVert, contentDescription = null) },
            text = { Text("Transfer") }
        )
        FloatingActionButtonMenuItem(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                expanded = false
                onTransactionPress()
            },
            icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
            text = { Text("Transaction") }
        )
    }
}
