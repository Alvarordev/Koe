package com.hazard.koe.domain.usecase.account

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CalculateCreditCardBillingCycleUseCaseTest {

    private val zoneId = ZoneId.of("UTC")
    private val useCase = CalculateCreditCardBillingCycleUseCase()

    @Test
    fun withClosingDay_transactionBeforeClose_staysInSameMonthCycle() {
        val txDate = LocalDate.of(2026, 1, 10).toMillis()

        val result = useCase(
            transactionDateMillis = txDate,
            paymentDay = 5,
            closingDay = 15,
            zoneId = zoneId
        )

        assertEquals(LocalDate.of(2025, 12, 16).toMillis(), result.cycleStartDate)
        assertEquals(LocalDate.of(2026, 1, 15).toMillis(), result.cycleCloseDate)
        assertEquals(LocalDate.of(2026, 2, 5).toMillis(), result.dueDate)
    }

    @Test
    fun withClosingDay_transactionAfterClose_movesToNextMonthCycle() {
        val txDate = LocalDate.of(2026, 1, 28).toMillis()

        val result = useCase(
            transactionDateMillis = txDate,
            paymentDay = 10,
            closingDay = 15,
            zoneId = zoneId
        )

        assertEquals(LocalDate.of(2026, 1, 16).toMillis(), result.cycleStartDate)
        assertEquals(LocalDate.of(2026, 2, 15).toMillis(), result.cycleCloseDate)
        assertEquals(LocalDate.of(2026, 3, 10).toMillis(), result.dueDate)
    }

    @Test
    fun withoutClosingDay_usesCalendarMonthFallback() {
        val txDate = LocalDate.of(2026, 3, 7).toMillis()

        val result = useCase(
            transactionDateMillis = txDate,
            paymentDay = 20,
            closingDay = null,
            zoneId = zoneId
        )

        assertEquals(LocalDate.of(2026, 3, 1).toMillis(), result.cycleStartDate)
        assertEquals(LocalDate.of(2026, 3, 31).toMillis(), result.cycleCloseDate)
        assertEquals(LocalDate.of(2026, 4, 20).toMillis(), result.dueDate)
    }

    @Test
    fun clampsPaymentDayForShortMonth() {
        val txDate = LocalDate.of(2026, 1, 10).toMillis()

        val result = useCase(
            transactionDateMillis = txDate,
            paymentDay = 31,
            closingDay = 20,
            zoneId = zoneId
        )

        assertEquals(LocalDate.of(2026, 2, 28).toMillis(), result.dueDate)
    }

    private fun LocalDate.toMillis(): Long =
        atStartOfDay(zoneId).toInstant().toEpochMilli()
}
