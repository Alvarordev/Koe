package com.hazard.koe.presentation.accounts

import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.FormalLoan
import com.hazard.koe.data.model.UserSubscription
import java.time.LocalDate

data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
    val casualLoanSummaries: List<CasualLoanSummaryWithPerson> = emptyList(),
    val formalLoans: List<FormalLoan> = emptyList(),
    val upcomingItems: List<UpcomingItem> = emptyList(),
    val isLoading: Boolean = true
)

data class CasualLoanSummaryWithPerson(
    val personId: Long,
    val personName: String,
    val personEmoji: String?,
    val totalLend: Long,
    val totalBorrow: Long
)

sealed interface UpcomingItem {
    val dueDate: LocalDate
    val daysRemaining: Int

    data class CreditPayment(
        val account: Account,
        override val dueDate: LocalDate,
        override val daysRemaining: Int
    ) : UpcomingItem

    data class SubscriptionBilling(
        val subscription: UserSubscription,
        val serviceName: String,
        val serviceColor: String?,
        val serviceIconResName: String?,
        val amount: Long,
        val currencyCode: String,
        override val dueDate: LocalDate,
        override val daysRemaining: Int
    ) : UpcomingItem
}
