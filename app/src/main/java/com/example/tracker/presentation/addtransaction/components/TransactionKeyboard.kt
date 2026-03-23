package com.example.tracker.presentation.addtransaction.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.example.tracker.presentation.addtransaction.KeyboardKey

private val KEY_HEIGHT = 56.dp

@Composable
fun TransactionKeyboard(
    onKey: (KeyboardKey) -> Unit,
    onSubmit: () -> Unit,
    onCurrencySelect: () -> Unit,
    isSubmitting: Boolean,
    isLocationEnabled: Boolean,
    onLocationToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    fun fireKeyTap() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun fireConfirm() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().height(KEY_HEIGHT)) {
            listOf('7', '8', '9').forEach { digit ->
                TextButton(
                    onClick = {
                        fireKeyTap()
                        onKey(KeyboardKey.Digit(digit))
                    },
                    modifier = Modifier.weight(1f).height(KEY_HEIGHT)
                ) {
                    Text(text = digit.toString(), style = MaterialTheme.typography.titleLarge)
                }
            }
            TextButton(
                onClick = {
                    fireKeyTap()
                    onKey(KeyboardKey.Delete)
                },
                modifier = Modifier.weight(1f).height(KEY_HEIGHT)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Delete"
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth().height(KEY_HEIGHT)) {
            listOf('4', '5', '6').forEach { digit ->
                TextButton(
                    onClick = {
                        fireKeyTap()
                        onKey(KeyboardKey.Digit(digit))
                    },
                    modifier = Modifier.weight(1f).height(KEY_HEIGHT)
                ) {
                    Text(text = digit.toString(), style = MaterialTheme.typography.titleLarge)
                }
            }
            IconToggleButton(
                checked = isLocationEnabled,
                onCheckedChange = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onLocationToggle()
                },
                modifier = Modifier.weight(1f).height(KEY_HEIGHT)
            ) {
                Icon(
                    imageVector = if (isLocationEnabled) Icons.Filled.LocationOn else Icons.Outlined.LocationOff,
                    contentDescription = null,
                    tint = if (isLocationEnabled) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth().height(KEY_HEIGHT * 2)) {
            Column(modifier = Modifier.weight(3f)) {
                Row(modifier = Modifier.fillMaxWidth().height(KEY_HEIGHT)) {
                    listOf('1', '2', '3').forEach { digit ->
                        TextButton(
                            onClick = {
                                fireKeyTap()
                                onKey(KeyboardKey.Digit(digit))
                            },
                            modifier = Modifier.weight(1f).height(KEY_HEIGHT)
                        ) {
                            Text(text = digit.toString(), style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().height(KEY_HEIGHT)) {
                    TextButton(
                        onClick = {
                            fireKeyTap()
                            onCurrencySelect()
                        },
                        modifier = Modifier.weight(1f).height(KEY_HEIGHT)
                    ) {
                        Text(text = "\u00A4", style = MaterialTheme.typography.titleLarge)
                    }
                    TextButton(
                        onClick = {
                            fireKeyTap()
                            onKey(KeyboardKey.Digit('0'))
                        },
                        modifier = Modifier.weight(1f).height(KEY_HEIGHT)
                    ) {
                        Text(text = "0", style = MaterialTheme.typography.titleLarge)
                    }
                    TextButton(
                        onClick = {
                            fireKeyTap()
                            onKey(KeyboardKey.Dot)
                        },
                        modifier = Modifier.weight(1f).height(KEY_HEIGHT)
                    ) {
                        Text(text = ".", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
            FilledTonalButton(
                onClick = {
                    if (!isSubmitting) {
                        fireConfirm()
                        onSubmit()
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .weight(1f)
                    .height(KEY_HEIGHT * 2)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Submit"
                )
            }
        }
    }
}
