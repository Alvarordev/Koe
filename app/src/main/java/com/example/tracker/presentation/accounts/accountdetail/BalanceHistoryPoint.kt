package com.example.tracker.presentation.accounts.accountdetail

import java.time.LocalDateTime

data class BalanceHistoryPoint(
    val dateTime: LocalDateTime,
    val balanceMinor: Long
)
