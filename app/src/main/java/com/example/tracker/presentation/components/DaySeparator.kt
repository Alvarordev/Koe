package com.example.tracker.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DaySeparator(date: LocalDate, modifier: Modifier = Modifier) {
    val today = LocalDate.now()
    val locale = Locale("es", "ES")
    
    val displayText = if (date == today) {
        "Hoy"
    } else {
        val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, locale)
            .replaceFirstChar { it.uppercase() }
        "$monthName ${date.dayOfMonth}"
    }

    Text(
        text = displayText,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(top = 20.dp, bottom = 4.dp)
    )
}
