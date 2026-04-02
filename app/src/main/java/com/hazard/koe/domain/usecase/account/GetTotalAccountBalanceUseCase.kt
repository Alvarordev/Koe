package com.hazard.koe.domain.usecase.account

import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.preferences.ExchangeRatePreferences
import com.hazard.koe.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTotalAccountBalanceUseCase(
    private val repository: AccountRepository,
    private val exchangeRatePreferences: ExchangeRatePreferences
) {
    operator fun invoke(): Flow<Long> = repository.getAll().map { accounts ->
        calculateTotalAccountBalanceInPen(accounts) { fromCurrency, toCurrency ->
            exchangeRatePreferences.getRate(fromCurrency, toCurrency)
        }
    }
}

internal suspend fun calculateTotalAccountBalanceInPen(
    accounts: List<Account>,
    getRate: suspend (fromCurrency: String, toCurrency: String) -> Double
): Long {
    return accounts.sumOf { account ->
        val sourceAmount = when (account.type) {
            AccountType.CREDIT -> {
                val creditLimit = account.creditLimit ?: 0L
                val creditUsed = account.creditUsed ?: 0L
                (creditLimit - creditUsed).coerceAtLeast(0L)
            }
            else -> account.currentBalance
        }

        val rate = getRate(account.currencyCode, "PEN")
        (sourceAmount * rate).toLong()
    }
}
