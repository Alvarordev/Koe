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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tracker.presentation.components.EmojiText

private val emojiGroups = linkedMapOf(
    "Food & Drink" to listOf(
        "\uD83C\uDF54", "\uD83C\uDF55", "\uD83C\uDF63", "\u2615", "\uD83C\uDF7A",
        "\uD83E\uDD57", "\uD83C\uDF70", "\uD83D\uDED2", "\uD83E\uDD5B", "\uD83C\uDF73",
        "\uD83C\uDF2E", "\uD83C\uDF5C", "\uD83C\uDF66", "\uD83E\uDD64", "\uD83C\uDF77",
        "\uD83E\uDD50", "\uD83E\uDDC1", "\uD83C\uDF4E", "\uD83E\uDD51", "\uD83C\uDF69",
        "\uD83E\uDDC3", "\uD83E\uDD69", "\uD83C\uDF72", "\uD83E\uDDC0", "\uD83E\uDD5A",
        "\uD83C\uDF5E", "\uD83C\uDF5D", "\uD83C\uDF5F", "\uD83C\uDF64", "\uD83C\uDF71",
        "\uD83C\uDF53", "\uD83C\uDF52", "\uD83C\uDF51", "\uD83C\uDF4D", "\uD83C\uDF4C",
        "\uD83E\uDD65", "\uD83C\uDF36\uFE0F", "\uD83C\uDF3D", "\uD83E\uDD6D", "\uD83C\uDF7B"
    ),
    "Transport" to listOf(
        "\uD83D\uDE97", "\uD83D\uDE8C", "\u2708\uFE0F", "\uD83D\uDE82", "\uD83D\uDEB2",
        "\u26FD", "\uD83D\uDE95", "\uD83D\uDEF5", "\uD83D\uDE87", "\uD83D\uDEF3\uFE0F",
        "\uD83C\uDFCD\uFE0F", "\uD83D\uDE81", "\uD83D\uDEF4", "\uD83D\uDE90", "\uD83D\uDE9A",
        "\uD83D\uDE99", "\uD83D\uDE9B", "\uD83D\uDE8D", "\uD83D\uDE8E", "\uD83D\uDE9D",
        "\uD83D\uDE9E", "\uD83D\uDE8B", "\uD83D\uDE86", "\uD83D\uDE85", "\uD83D\uDE84",
        "\uD83D\uDE83", "\uD83D\uDEA2", "\u26F5", "\uD83D\uDEA0", "\uD83D\uDEA1",
        "\uD83D\uDE80", "\uD83D\uDEEB", "\uD83D\uDEEC", "\uD83D\uDEF6", "\uD83D\uDE94"
    ),
    "Home & Life" to listOf(
        "\uD83C\uDFE0", "\uD83D\uDCA1", "\uD83D\uDECF\uFE0F", "\uD83E\uDDF9", "\uD83D\uDD11",
        "\uD83D\uDCE1", "\uD83D\uDEC1", "\uD83E\uDEB4", "\uD83E\uDDFA", "\uD83C\uDFD7\uFE0F",
        "\uD83E\uDE91", "\uD83D\uDECB\uFE0F", "\uD83D\uDEBF", "\uD83E\uDDF2", "\uD83D\uDD27",
        "\uD83C\uDFE1", "\uD83C\uDFDA\uFE0F", "\uD83C\uDFD8\uFE0F", "\uD83C\uDFD9\uFE0F", "\uD83E\uDDF1",
        "\uD83D\uDEAA", "\uD83E\uDE9F", "\uD83D\uDEBD", "\uD83E\uDDFD", "\uD83E\uDDF3",
        "\uD83D\uDD0C", "\uD83D\uDEE0\uFE0F", "\uD83E\uDEA3", "\uD83E\uDDF4", "\uD83E\uDDEF",
        "\uD83D\uDD26", "\uD83E\uDDEA", "\uD83E\uDE92", "\uD83E\uDEAE", "\uD83D\uDD0D"
    ),
    "Fun & Media" to listOf(
        "\uD83C\uDFAC", "\uD83C\uDFAE", "\uD83C\uDFB5", "\uD83D\uDCDA", "\uD83C\uDFAD",
        "\uD83C\uDFBF", "\u26BD", "\uD83C\uDFB8", "\uD83C\uDFAF", "\uD83C\uDFB2",
        "\uD83C\uDFAA", "\uD83C\uDFCB\uFE0F", "\uD83C\uDFA8", "\uD83D\uDCF8", "\uD83C\uDFBB",
        "\uD83C\uDFBA", "\uD83E\uDD41", "\uD83C\uDFB9", "\uD83C\uDFB6", "\uD83C\uDFA4",
        "\uD83C\uDFB3", "\uD83C\uDFC0", "\uD83C\uDFC8", "\u26BE", "\uD83C\uDFBE",
        "\uD83C\uDFB1", "\uD83C\uDFC4", "\uD83C\uDFC2", "\uD83E\uDD3A", "\uD83C\uDFC6",
        "\uD83D\uDCFA", "\uD83D\uDCFB", "\uD83C\uDFA5", "\uD83D\uDCF7", "\uD83C\uDFAE"
    ),
    "Finance & Work" to listOf(
        "\uD83D\uDCBC", "\uD83D\uDCB0", "\uD83D\uDCC8", "\uD83D\uDCB3", "\uD83C\uDFE6",
        "\uD83D\uDCCA", "\uD83D\uDCB5", "\uD83C\uDFE2", "\uD83D\uDCF1", "\uD83D\uDCBB",
        "\uD83D\uDCE7", "\uD83D\uDD12", "\uD83D\uDCCB", "\uD83C\uDF93", "\uD83C\uDFE5",
        "\uD83D\uDCB6", "\uD83D\uDCB7", "\uD83D\uDCB8", "\uD83D\uDCB9", "\uD83D\uDCB2",
        "\uD83D\uDCC9", "\uD83D\uDCDD", "\uD83D\uDCC5", "\uD83D\uDCC6", "\uD83D\uDCC4",
        "\uD83D\uDCC3", "\uD83D\uDCCE", "\uD83D\uDCC7", "\uD83D\uDCC1", "\uD83D\uDCC2",
        "\uD83D\uDDA8\uFE0F", "\u2328\uFE0F", "\uD83D\uDDA5\uFE0F", "\uD83D\uDCBE", "\uD83C\uDFE3"
    ),
    "Nature & Weather" to listOf(
        "\uD83C\uDF1E", "\uD83C\uDF19", "\u2B50", "\uD83C\uDF08", "\u2601\uFE0F",
        "\u26C8\uFE0F", "\uD83C\uDF2A\uFE0F", "\u2744\uFE0F", "\uD83C\uDF0A", "\uD83C\uDF3F",
        "\uD83C\uDF32", "\uD83C\uDF33", "\uD83C\uDF34", "\uD83C\uDF35", "\uD83C\uDF3B",
        "\uD83C\uDF3A", "\uD83C\uDF39", "\uD83C\uDF37", "\uD83C\uDF38", "\uD83C\uDF3C",
        "\uD83C\uDF1A", "\uD83C\uDF1B", "\uD83C\uDF1D", "\u2600\uFE0F", "\uD83C\uDF24\uFE0F",
        "\uD83C\uDF27\uFE0F", "\uD83C\uDF28\uFE0F", "\uD83C\uDF0D", "\uD83C\uDF0B", "\uD83C\uDFDD\uFE0F",
        "\uD83C\uDF05", "\uD83C\uDF04", "\uD83D\uDD25", "\uD83C\uDF0C", "\uD83C\uDF43"
    ),
    "People & Gestures" to listOf(
        "\uD83D\uDE00", "\uD83D\uDE02", "\uD83E\uDD23", "\uD83D\uDE0D", "\uD83E\uDD70",
        "\uD83D\uDE0E", "\uD83E\uDD13", "\uD83E\uDD29", "\uD83D\uDE4F", "\uD83D\uDC4D",
        "\uD83D\uDC4E", "\uD83D\uDC4B", "\u270C\uFE0F", "\uD83E\uDD1E", "\uD83D\uDC4C",
        "\uD83D\uDCAA", "\uD83D\uDC68\u200D\uD83D\uDCBB", "\uD83D\uDC69\u200D\uD83D\uDCBB", "\uD83D\uDC68\u200D\uD83C\uDF73", "\uD83D\uDC69\u200D\uD83C\uDF93",
        "\uD83D\uDC68\u200D\uD83C\uDFEB", "\uD83D\uDC69\u200D\u2695\uFE0F", "\uD83D\uDC6A", "\uD83D\uDC91", "\uD83E\uDDD1\u200D\uD83D\uDE80",
        "\uD83E\uDD37", "\uD83D\uDE4C", "\uD83D\uDE4B", "\uD83E\uDD26", "\uD83D\uDC68\u200D\uD83D\uDD27",
        "\uD83E\uDDD8", "\uD83C\uDFC3", "\uD83D\uDEB6", "\uD83D\uDC76", "\uD83D\uDC75"
    ),
    "Shopping & Fashion" to listOf(
        "\uD83D\uDECD\uFE0F", "\uD83D\uDEF7", "\uD83D\uDC5C", "\uD83D\uDC5B", "\uD83C\uDF92",
        "\uD83D\uDC5E", "\uD83D\uDC5F", "\uD83D\uDC60", "\uD83D\uDC61", "\uD83D\uDC62",
        "\uD83D\uDC57", "\uD83D\uDC58", "\uD83E\uDDE3", "\uD83E\uDDE4", "\uD83E\uDDE5",
        "\uD83D\uDC59", "\uD83D\uDC54", "\uD83D\uDC55", "\uD83D\uDC56", "\uD83E\uDE73",
        "\uD83E\uDDE6", "\uD83D\uDC53", "\uD83D\uDD76\uFE0F", "\uD83C\uDF80", "\uD83E\uDDE2",
        "\uD83D\uDC84", "\uD83D\uDC8D", "\uD83D\uDC8E", "\uD83D\uDC51", "\uD83E\uDE74",
        "\uD83D\uDC9C", "\uD83D\uDECD\uFE0F", "\uD83C\uDF81", "\uD83C\uDFF7\uFE0F", "\uD83E\uDDF5"
    ),
    "Other" to listOf(
        "\uD83C\uDF81", "\u2728", "\uD83D\uDC3E", "\uD83D\uDC76", "\uD83D\uDC8A",
        "\uD83D\uDEE1\uFE0F", "\uD83D\uDD01", "\uD83D\uDCE6", "\uD83D\uDECD\uFE0F", "\uD83D\uDC8E",
        "\uD83E\uDDF8", "\uD83E\uDEA5", "\uD83D\uDC57", "\uD83D\uDC87", "\uD83E\uDDF4",
        "\uD83D\uDC36", "\uD83D\uDC31", "\uD83D\uDC2D", "\uD83D\uDC30", "\uD83E\uDD8A",
        "\uD83D\uDC3B", "\uD83D\uDC28", "\uD83D\uDC2F", "\uD83E\uDD81", "\uD83D\uDC37",
        "\uD83D\uDC38", "\uD83D\uDC22", "\uD83D\uDC0D", "\uD83D\uDC1D", "\uD83E\uDD8B",
        "\u2764\uFE0F", "\uD83D\uDC9A", "\uD83D\uDC99", "\uD83D\uDD4A\uFE0F", "\u267B\uFE0F"
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
                            EmojiText(text = emoji, style = TextStyle(fontSize = 24.sp))
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
