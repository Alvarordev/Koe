package com.hazard.koe.domain.usecase.account

import com.hazard.koe.data.preferences.ExchangeRatePreferences
import com.hazard.koe.domain.repository.AccountRepository
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
