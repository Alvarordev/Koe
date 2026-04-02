package com.hazard.koe.domain.usecase.account

import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.data.model.Transaction
import com.hazard.koe.domain.repository.AccountRepository
import com.hazard.koe.domain.repository.TransactionRepository
import com.hazard.koe.presentation.accounts.accountdetail.BalanceHistoryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class GetBalanceHistoryUseCase(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(accountId: Long): Flow<List<BalanceHistoryPoint>> = combine(
        accountRepository.getById(accountId),
        transactionRepository.getByAccount(accountId)
    ) { account, transactions ->
        if (account == null) return@combine emptyList()

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)

        val currentDisplayedBalance = if (account.type == AccountType.CREDIT) {
            (account.creditLimit ?: 0L) - (account.creditUsed ?: 0L)
        } else {
            account.currentBalance
        }

        if (transactions.isEmpty()) return@combine emptyList()

        val deltasByDate = transactions
            .groupBy { txWithDetails ->
                Instant.ofEpochMilli(txWithDetails.transaction.date)
                    .atZone(zone)
                    .toLocalDate()
            }
            .mapValues { (_, txnsOnDay) ->
                txnsOnDay.sumOf { txWithDetails ->
                    deltaForAccount(
                        transaction = txWithDetails.transaction,
                        accountId = accountId,
                        accountType = account.type
                    )
                }
            }

        val earliestDate = deltasByDate.keys.minOrNull() ?: today
        if (earliestDate.isAfter(today)) return@combine emptyList()

        var runningEndOfDayBalance = currentDisplayedBalance
        val points = mutableListOf<BalanceHistoryPoint>()
        var date = today

        while (!date.isBefore(earliestDate)) {
            points += BalanceHistoryPoint(
                dateMillis = date.atStartOfDay(zone).toInstant().toEpochMilli(),
                balance = runningEndOfDayBalance
            )

            val dayDelta = deltasByDate[date] ?: 0L
            runningEndOfDayBalance -= dayDelta
            date = date.minusDays(1)
        }

        points.asReversed()
    }

    private fun deltaForAccount(
        transaction: Transaction,
        accountId: Long,
        accountType: AccountType
    ): Long {
        return if (accountType == AccountType.CREDIT) {
            when (transaction.type) {
                TransactionType.EXPENSE -> {
                    if (transaction.accountId == accountId) -transaction.amount else 0L
                }

                TransactionType.CREDIT_CARD_PAYMENT -> {
                    if (transaction.transferToAccountId == accountId) transaction.amount else 0L
                }

                TransactionType.INCOME,
                TransactionType.TRANSFER -> 0L
            }
        } else {
            when (transaction.type) {
                TransactionType.EXPENSE -> {
                    if (transaction.accountId == accountId) -transaction.amount else 0L
                }

                TransactionType.INCOME -> {
                    if (transaction.accountId == accountId) transaction.amount else 0L
                }

                TransactionType.TRANSFER -> {
                    when {
                        transaction.accountId == accountId -> -transaction.amount
                        transaction.transferToAccountId == accountId -> transaction.convertedAmount ?: transaction.amount
                        else -> 0L
                    }
                }

                TransactionType.CREDIT_CARD_PAYMENT -> {
                    if (transaction.accountId == accountId) -transaction.amount else 0L
                }
            }
        }
    }
}
