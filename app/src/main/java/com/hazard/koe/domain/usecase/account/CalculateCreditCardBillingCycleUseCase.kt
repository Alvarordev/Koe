package com.hazard.koe.domain.usecase.account

import com.hazard.koe.domain.model.CreditCardBillingCycle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CalculateCreditCardBillingCycleUseCase {

    operator fun invoke(
        transactionDateMillis: Long,
        paymentDay: Int,
        closingDay: Int?,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): CreditCardBillingCycle {
        require(paymentDay in 1..31) { "Payment day must be between 1 and 31" }
        require(closingDay == null || closingDay in 1..31) { "Closing day must be between 1 and 31" }

        val txDate = Instant.ofEpochMilli(transactionDateMillis).atZone(zoneId).toLocalDate()

        return if (closingDay == null) {
            val cycleStart = txDate.withDayOfMonth(1)
            val cycleClose = txDate.withDayOfMonth(txDate.lengthOfMonth())
            val dueMonth = txDate.plusMonths(1)
            val dueDate = dueMonth.withDayOfMonth(paymentDay.coerceAtMost(dueMonth.lengthOfMonth()))

            CreditCardBillingCycle(
                cycleStartDate = cycleStart.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                cycleCloseDate = cycleClose.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                dueDate = dueDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
            )
        } else {
            val closeDate = if (txDate.dayOfMonth <= closingDay) {
                txDate.withDayOfMonth(closingDay.coerceAtMost(txDate.lengthOfMonth()))
            } else {
                val nextMonth = txDate.plusMonths(1)
                nextMonth.withDayOfMonth(closingDay.coerceAtMost(nextMonth.lengthOfMonth()))
            }

            val previousCloseDate = closeDate.minusMonths(1)
            val cycleStart = previousCloseDate.plusDays(1)
            val dueMonth = closeDate.plusMonths(1)
            val dueDate = dueMonth.withDayOfMonth(paymentDay.coerceAtMost(dueMonth.lengthOfMonth()))

            CreditCardBillingCycle(
                cycleStartDate = cycleStart.toStartOfDayMillis(zoneId),
                cycleCloseDate = closeDate.toStartOfDayMillis(zoneId),
                dueDate = dueDate.toStartOfDayMillis(zoneId)
            )
        }
    }

    private fun LocalDate.toStartOfDayMillis(zoneId: ZoneId): Long =
        atStartOfDay(zoneId).toInstant().toEpochMilli()
}
