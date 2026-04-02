package com.hazard.koe.domain.model

data class CreditCardBillingCycle(
    val cycleStartDate: Long,
    val cycleCloseDate: Long,
    val dueDate: Long
)
