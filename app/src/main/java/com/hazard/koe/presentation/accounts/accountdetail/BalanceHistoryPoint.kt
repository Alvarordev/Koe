package com.hazard.koe.presentation.accounts.accountdetail

import java.time.LocalDateTime

data class BalanceHistoryPoint(
    val dateMillis: Long,
    val balance: Long
)

