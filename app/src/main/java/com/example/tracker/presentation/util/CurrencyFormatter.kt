package com.example.tracker.presentation.util

import com.example.tracker.data.enums.SupportedCurrency
import com.example.tracker.data.enums.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object CurrencyFormatter {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.forLanguageTag("es"))

    fun formatBalance(amountMinor: Long, currencyCode: String): String {
        val symbol = SupportedCurrency.entries.find { it.code == currencyCode }?.symbol ?: currencyCode
        return "$symbol ${String.format("%.2f", amountMinor / 100.0)}"
    }

    fun formatAmount(amountMinor: Long, currencyCode: String, type: TransactionType): String {
        val symbol = SupportedCurrency.entries
            .find { it.code == currencyCode }
            ?.symbol ?: currencyCode

        val value = amountMinor / 100.0
        val formatted = String.format("%.2f", value)

        return when (type) {
            TransactionType.EXPENSE -> "- $symbol$formatted"
            TransactionType.INCOME -> "+ $symbol$formatted"
            else -> "$symbol$formatted"
        }
    }

    fun formatTime(epochMillis: Long): String {
        val time = Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
        return timeFormatter.format(time)
    }

    fun formatDateTime(epochMillis: Long): String {
        val zonedDateTime = Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
        return dateTimeFormatter.format(zonedDateTime)
    }

    fun toLocalDate(epochMillis: Long): LocalDate {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }
}
