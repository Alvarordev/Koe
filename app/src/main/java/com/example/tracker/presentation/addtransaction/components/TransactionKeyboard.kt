package com.example.tracker.presentation.addtransaction.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(8.dp)
    ) {
        val col1 = listOf('1', '4', '7', '$')
        val col2 = listOf('2', '5', '8', '0')
        val col3 = listOf('3', '6', '9', '.')

        val columns = listOf(col1, col2, col3)

        columns.forEach { col ->
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                col.forEach { key ->
                    NumpadButton(
                        text = key.toString(),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            when (key) {
                                '$' -> onCurrencySelect()
                                '.' -> onKey(
                                    KeyboardKey.Dot
                                )
                                else -> onKey(KeyboardKey.Digit(key))
                            }
                        },
                        isSecondary = key == '$'
                    )
                }
            }


        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onKey(KeyboardKey.Delete)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onLocationToggle()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLocationEnabled) Icons.Filled.LocationOn else Icons.Outlined.LocationOff,
                    contentDescription = "Location toggle",
                    tint = if (isLocationEnabled) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        if (!isSubmitting) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onSubmit()
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Guardar",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

//    Column(modifier = modifier.fillMaxWidth()) {
//        Row(modifier = Modifier.fillMaxWidth().height(KEY_HEIGHT)) {
//            listOf('7', '8', '9').forEach { digit ->
//                TextButton(
//                    onClick = {
//                        fireKeyTap()
//                        onKey(KeyboardKey.Digit(digit))
//                    },
//                    modifier = Modifier.weight(1f).height(KEY_HEIGHT)
//                ) {
//                    Text(text = digit.toString(), style = MaterialTheme.typography.titleLarge)
//                }
//            }
//            TextButton(
//                onClick = {
//                    fireKeyTap()
//                    onKey(KeyboardKey.Delete)
//                },
//                modifier = Modifier.weight(1f).height(KEY_HEIGHT)
//            ) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.Backspace,
//                    contentDescription = "Delete"
//                )
//            }
//        }
//
//        Row(modifier = Modifier.fillMaxWidth().height(KEY_HEIGHT)) {
//            listOf('4', '5', '6').forEach { digit ->
//                TextButton(
//                    onClick = {
//                        fireKeyTap()
//                        onKey(KeyboardKey.Digit(digit))
//                    },
//                    modifier = Modifier.weight(1f).height(KEY_HEIGHT)
//                ) {
//                    Text(text = digit.toString(), style = MaterialTheme.typography.titleLarge)
//                }
//            }
//            IconToggleButton(
//                checked = isLocationEnabled,
//                onCheckedChange = {
//                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
//                    onLocationToggle()
//                },
//                modifier = Modifier.weight(1f).height(KEY_HEIGHT)
//            ) {
//                Icon(
//                    imageVector = if (isLocationEnabled) Icons.Filled.LocationOn else Icons.Outlined.LocationOff,
//                    contentDescription = null,
//                    tint = if (isLocationEnabled) MaterialTheme.colorScheme.primary else LocalContentColor.current
//                )
//            }
//        }
//
//        Row(modifier = Modifier.fillMaxWidth().height(KEY_HEIGHT * 2)) {
//            Column(modifier = Modifier.weight(3f)) {
//                Row(modifier = Modifier.fillMaxWidth().height(KEY_HEIGHT)) {
//                    listOf('1', '2', '3').forEach { digit ->
//                        TextButton(
//                            onClick = {
//                                fireKeyTap()
//                                onKey(KeyboardKey.Digit(digit))
//                            },
//                            modifier = Modifier.weight(1f).height(KEY_HEIGHT)
//                        ) {
//                            Text(text = digit.toString(), style = MaterialTheme.typography.titleLarge)
//                        }
//                    }
//                }
//                Row(modifier = Modifier.fillMaxWidth().height(KEY_HEIGHT)) {
//                    TextButton(
//                        onClick = {
//                            fireKeyTap()
//                            onCurrencySelect()
//                        },
//                        modifier = Modifier.weight(1f).height(KEY_HEIGHT)
//                    ) {
//                        Text(text = "\u00A4", style = MaterialTheme.typography.titleLarge)
//                    }
//                    TextButton(
//                        onClick = {
//                            fireKeyTap()
//                            onKey(KeyboardKey.Digit('0'))
//                        },
//                        modifier = Modifier.weight(1f).height(KEY_HEIGHT)
//                    ) {
//                        Text(text = "0", style = MaterialTheme.typography.titleLarge)
//                    }
//                    TextButton(
//                        onClick = {
//                            fireKeyTap()
//                            onKey(KeyboardKey.Dot)
//                        },
//                        modifier = Modifier.weight(1f).height(KEY_HEIGHT)
//                    ) {
//                        Text(text = ".", style = MaterialTheme.typography.titleLarge)
//                    }
//                }
//            }
//            FilledTonalButton(
//                onClick = {
//                    if (!isSubmitting) {
//                        fireConfirm()
//                        onSubmit()
//                    }
//                },
//                enabled = !isSubmitting,
//                modifier = Modifier
//                    .weight(1f)
//                    .height(KEY_HEIGHT * 2)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Check,
//                    contentDescription = "Submit"
//                )
//            }
//        }
//    }
}

@Composable
fun NumpadButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isSecondary: Boolean = false
) {
    val view = LocalView.current

    Box(
        modifier = modifier
            .aspectRatio(1.3f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (isSecondary) 16.sp else 24.sp,
            fontWeight = if (isSecondary) FontWeight.Medium else FontWeight.SemiBold,
            color = if (isSecondary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
        )
    }
}
