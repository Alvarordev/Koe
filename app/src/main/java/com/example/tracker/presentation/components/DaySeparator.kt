package com.example.tracker.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun DaySeparator(date: LocalDate, modifier: Modifier = Modifier) {

    val localSpanish = Locale("es", "ES")

    Text(
        text = "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.FULL, LocalLocale.current.platformLocale)}",
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(top = 20.dp, bottom = 4.dp)
    )
}
