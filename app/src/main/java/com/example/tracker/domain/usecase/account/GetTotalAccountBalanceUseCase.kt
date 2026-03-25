package com.example.tracker.domain.usecase.account

import com.example.tracker.data.preferences.ExchangeRatePreferences
import com.example.tracker.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTotalAccountBalanceUseCase(
    private val repository: AccountRepository,
    private val exchangeRatePreferences: ExchangeRatePreferences
) {
    operator fun invoke(): Flow<Long> = repository.getTotalBalanceByCurrency().map { balances ->
        balances.sumOf { cb ->
            val rate = exchangeRatePreferences.getRate(cb.currencyCode, "PEN")
            (cb.totalBalance * rate).toLong()
        }
    }
}
