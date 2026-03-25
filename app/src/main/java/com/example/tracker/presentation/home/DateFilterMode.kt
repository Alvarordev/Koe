package com.example.tracker.presentation.home

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

sealed interface DateFilterMode {

    fun dateRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        return when (this) {
            is Today -> {
                val now = LocalDate.now()
                val start = now.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = now.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
                start to end
            }
            is Week -> {
                val now = LocalDate.now()
                val monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val sunday = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                val start = monday.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = sunday.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
                start to end
            }
            is Month -> {
                val now = LocalDate.now()
                val first = now.withDayOfMonth(1)
                val lastDay = now.with(TemporalAdjusters.lastDayOfMonth())
                val start = first.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = lastDay.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
                start to end
            }
            is SpecificDate -> {
                val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
                start to end
            }
            is DateRange -> {
                val start = this.start.atStartOfDay(zone).toInstant().toEpochMilli()
                val end = this.end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
                start to end
            }
        }
    }

    fun label(): String {
        val spanishLocale = Locale.Builder().setLanguage("es").setRegion("PE").build()
        return when (this) {
            is Today -> {
                val fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy", spanishLocale)
                LocalDate.now().format(fmt).replaceFirstChar { it.uppercase() }
            }
            is Week -> {
                val now = LocalDate.now()
                val monday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val sunday = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                val fmt = DateTimeFormatter.ofPattern("MMM dd", spanishLocale)
                "${monday.format(fmt).replaceFirstChar { it.uppercase() }} — ${sunday.format(fmt).replaceFirstChar { it.uppercase() }}"
            }
            is Month -> {
                val fmt = DateTimeFormatter.ofPattern("MMMM yyyy", spanishLocale)
                LocalDate.now().format(fmt).replaceFirstChar { it.uppercase() }
            }
            is SpecificDate -> {
                val fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy", spanishLocale)
                date.format(fmt).replaceFirstChar { it.uppercase() }
            }
            is DateRange -> {
                val fmt = DateTimeFormatter.ofPattern("MMM dd yyyy", spanishLocale)
                "${start.format(fmt).replaceFirstChar { it.uppercase() }} — ${end.format(fmt).replaceFirstChar { it.uppercase() }}"
            }
        }
    }

    data object Today : DateFilterMode
    data object Week : DateFilterMode
    data object Month : DateFilterMode
    data class SpecificDate(val date: LocalDate) : DateFilterMode
    data class DateRange(val start: LocalDate, val end: LocalDate) : DateFilterMode
}
