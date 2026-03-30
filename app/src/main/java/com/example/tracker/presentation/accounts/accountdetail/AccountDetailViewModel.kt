package com.example.tracker.presentation.accounts.accountdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.enums.AccountType
import com.example.tracker.data.enums.TransactionType
import com.example.tracker.data.model.Account
import com.example.tracker.data.model.relations.TransactionWithDetails
import com.example.tracker.domain.usecase.account.ArchiveAccountUseCase
import com.example.tracker.domain.usecase.account.GetAccountByIdUseCase
import com.example.tracker.domain.usecase.transaction.GetTransactionsByAccountUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class AccountDetailViewModel(
    private val accountId: Long,
    getAccountById: GetAccountByIdUseCase,
    getTransactionsByAccount: GetTransactionsByAccountUseCase,
    private val archiveAccount: ArchiveAccountUseCase
) : ViewModel() {

    private val _showDeleteDialog = MutableStateFlow(false)
    private val _isArchived = MutableStateFlow(false)

    val uiState: StateFlow<AccountDetailUiState> = combine(
        getAccountById(accountId),
        getTransactionsByAccount(accountId),
        _showDeleteDialog,
        _isArchived
    ) { account, transactions, showDelete, archived ->
        AccountDetailUiState(
            account = account,
            transactions = transactions,
            balanceHistory = buildBalanceHistory(account, transactions),
            isLoading = false,
            showDeleteDialog = showDelete,
            isArchived = archived
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountDetailUiState()
    )

    fun showDeleteDialog() {
        _showDeleteDialog.value = true
    }

    fun dismissDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun archiveAccount() {
        viewModelScope.launch {
            archiveAccount(accountId)
            _isArchived.value = true
        }
    }

    private fun buildBalanceHistory(
        account: Account?,
        transactions: List<TransactionWithDetails>
    ): List<BalanceHistoryPoint> {
        if (account == null || account.type == AccountType.CREDIT) return emptyList()

        val zoneId = ZoneId.systemDefault()
        val currentTime = LocalDateTime.now(zoneId).truncatedToThirtyMinutes()
        val impactsByInterval = transactions
            .groupBy {
                Instant.ofEpochMilli(it.transaction.date)
                    .atZone(zoneId)
                    .toLocalDateTime()
                    .truncatedToThirtyMinutes()
            }
            .mapValues { (_, txns) ->
                txns.sumOf { transactionImpactForAccount(it, account.id) }
            }

        val earliestInterval = impactsByInterval.keys.minOrNull() ?: return emptyList()

        var runningBalance = account.currentBalance
        val historyDescending = mutableListOf<BalanceHistoryPoint>()
        var time = currentTime

        while (!time.isBefore(earliestInterval)) {
            historyDescending += BalanceHistoryPoint(
                dateTime = time,
                balanceMinor = runningBalance
            )
            runningBalance -= impactsByInterval[time] ?: 0L
            time = time.minusMinutes(30)
        }

        return if (historyDescending.size < 2) {
            emptyList()
        } else {
            historyDescending.asReversed()
        }
    }

    private fun LocalDateTime.truncatedToThirtyMinutes(): LocalDateTime {
        val adjustedMinute = (minute / 30) * 30
        return withMinute(adjustedMinute)
            .withSecond(0)
            .withNano(0)
    }

    private fun transactionImpactForAccount(
        transactionWithDetails: TransactionWithDetails,
        accountId: Long
    ): Long {
        val transaction = transactionWithDetails.transaction
        return when (transaction.type) {
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
