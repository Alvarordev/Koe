package com.example.tracker.presentation.categories.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val emojiGroups = linkedMapOf(
    "Food & Drink" to listOf(
        "\uD83C\uDF54", "\uD83C\uDF55", "\uD83C\uDF63", "\u2615", "\uD83C\uDF7A",
        "\uD83E\uDD57", "\uD83C\uDF70", "\uD83D\uDED2", "\uD83E\uDD5B", "\uD83C\uDF73",
        "\uD83C\uDF2E", "\uD83C\uDF5C", "\uD83C\uDF66", "\uD83E\uDD64", "\uD83C\uDF77",
        "\uD83E\uDD50", "\uD83E\uDDC1", "\uD83C\uDF4E", "\uD83E\uDD51", "\uD83C\uDF69",
        "\uD83E\uDDC3", "\uD83E\uDD69", "\uD83C\uDF72", "\uD83E\uDDC0", "\uD83E\uDD5A"
    ),
    "Transport" to listOf(
        "\uD83D\uDE97", "\uD83D\uDE8C", "\u2708\uFE0F", "\uD83D\uDE82", "\uD83D\uDEB2",
        "\u26FD", "\uD83D\uDE95", "\uD83D\uDEF5", "\uD83D\uDE87", "\uD83D\uDEF3\uFE0F",
        "\uD83C\uDFCD\uFE0F", "\uD83D\uDE81", "\uD83D\uDEF4", "\uD83D\uDE90", "\uD83D\uDE9A"
    ),
    "Home & Life" to listOf(
        "\uD83C\uDFE0", "\uD83D\uDCA1", "\uD83D\uDECF\uFE0F", "\uD83E\uDDF9", "\uD83D\uDD11",
        "\uD83D\uDCE1", "\uD83D\uDEC1", "\uD83E\uDEB4", "\uD83E\uDDFA", "\uD83C\uDFD7\uFE0F",
        "\uD83E\uDE91", "\uD83D\uDECB\uFE0F", "\uD83D\uDEBF", "\uD83E\uDDF2", "\uD83D\uDD27"
    ),
    "Fun & Media" to listOf(
        "\uD83C\uDFAC", "\uD83C\uDFAE", "\uD83C\uDFB5", "\uD83D\uDCDA", "\uD83C\uDFAD",
        "\uD83C\uDFBF", "\u26BD", "\uD83C\uDFB8", "\uD83C\uDFAF", "\uD83C\uDFB2",
        "\uD83C\uDFAA", "\uD83C\uDFCB\uFE0F", "\uD83C\uDFA8", "\uD83D\uDCF8", "\uD83C\uDFBB"
    ),
    "Finance & Work" to listOf(
        "\uD83D\uDCBC", "\uD83D\uDCB0", "\uD83D\uDCC8", "\uD83D\uDCB3", "\uD83C\uDFE6",
        "\uD83D\uDCCA", "\uD83D\uDCB5", "\uD83C\uDFE2", "\uD83D\uDCF1", "\uD83D\uDCBB",
        "\uD83D\uDCE7", "\uD83D\uDD12", "\uD83D\uDCCB", "\uD83C\uDF93", "\uD83C\uDFE5"
    ),
    "Other" to listOf(
        "\uD83C\uDF81", "\u2728", "\uD83D\uDC3E", "\uD83D\uDC76", "\uD83D\uDC8A",
        "\uD83D\uDEE1\uFE0F", "\uD83D\uDD01", "\uD83D\uDCE6", "\uD83D\uDECD\uFE0F", "\uD83D\uDC8E",
        "\uD83E\uDDF8", "\uD83E\uDEA5", "\uD83D\uDC57", "\uD83D\uDC87", "\uD83E\uDDF4"
    )
)

@Composable
fun EmojiPickerDialog(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick an Emoji") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                emojiGroups.forEach { (groupName, emojis) ->
                    item(span = { GridItemSpan(7) }) {
                        Text(
                            text = groupName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(emojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onEmojiSelected(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
