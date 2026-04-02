package com.hazard.koe.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.relations.SubscriptionWithDetails
import com.hazard.koe.domain.usecase.account.GetAccountsUseCase
import com.hazard.koe.domain.usecase.loan.GetCasualLoansUseCase
import com.hazard.koe.domain.usecase.loan.GetFormalLoansUseCase
import com.hazard.koe.domain.usecase.person.GetPersonsUseCase
import com.hazard.koe.domain.usecase.subscription.GetActiveSubscriptionsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class AccountsViewModel(
    getAccounts: GetAccountsUseCase,
    getCasualLoans: GetCasualLoansUseCase,
    getFormalLoans: GetFormalLoansUseCase,
    getPersons: GetPersonsUseCase,
    getActiveSubscriptions: GetActiveSubscriptionsUseCase
) : ViewModel() {

    val uiState: StateFlow<AccountsUiState> = combine(
        getAccounts(),
        getCasualLoans(),
        getFormalLoans(),
        getPersons(),
        getActiveSubscriptions()
    ) { accounts, casualLoans, formalLoans, persons, subscriptions ->
        val casualLoanSummaries = casualLoans
            .groupBy { it.loan.personId }
            .map { (personId, loans) ->
                val person = loans.firstOrNull()?.person
                val lendTotal = loans.filter { it.loan.direction == com.hazard.koe.data.enums.LoanDirection.LENT }
                    .sumOf { it.loan.outstandingBalance }
                val borrowTotal = loans.filter { it.loan.direction == com.hazard.koe.data.enums.LoanDirection.BORROWED }
                    .sumOf { it.loan.outstandingBalance }
                CasualLoanSummaryWithPerson(
                    personId = personId,
                    personName = person?.name ?: "Desconocido",
                    personEmoji = person?.emoji,
                    totalLend = lendTotal,
                    totalBorrow = borrowTotal
                )
            }

        val activeAccounts = accounts.filter { !it.isArchived }

        val creditPayments = computeUpcomingPayments(activeAccounts)
        val subscriptionBillings = computeUpcomingSubscriptions(subscriptions)
        val allUpcoming = (creditPayments + subscriptionBillings).sortedBy { it.dueDate }

        AccountsUiState(
            accounts = activeAccounts,
            casualLoanSummaries = casualLoanSummaries,
            formalLoans = formalLoans.filter { it.isActive },
            upcomingItems = allUpcoming,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountsUiState()
    )

    private fun computeUpcomingPayments(accounts: List<Account>): List<UpcomingItem.CreditPayment> {
        val today = LocalDate.now()
        return accounts
            .filter { it.type == AccountType.CREDIT && it.paymentDay != null }
            .map { account ->
                val paymentDay = account.paymentDay!!
                val dueDate = resolveNextDueDate(today, paymentDay)
                val daysRemaining = ChronoUnit.DAYS.between(today, dueDate).toInt()
                UpcomingItem.CreditPayment(
                    account = account,
                    dueDate = dueDate,
                    daysRemaining = daysRemaining
                )
            }
    }

    private fun computeUpcomingSubscriptions(
        subscriptions: List<SubscriptionWithDetails>
    ): List<UpcomingItem.SubscriptionBilling> {
        val today = LocalDate.now()
        return subscriptions.map { detail ->
            val sub = detail.subscription
            val dueDate = resolveNextDueDate(today, sub.billingDay)
            val daysRemaining = ChronoUnit.DAYS.between(today, dueDate).toInt()
            val name = sub.customName
                ?: detail.service?.name
                ?: "Suscripcion"
            UpcomingItem.SubscriptionBilling(
                subscription = sub,
                serviceName = name,
                serviceColor = detail.service?.color,
                serviceIconResName = sub.iconResName ?: detail.service?.iconResName,
                amount = sub.amount,
                currencyCode = sub.currencyCode,
                dueDate = dueDate,
                daysRemaining = daysRemaining
            )
        }
    }

    private fun resolveNextDueDate(today: LocalDate, day: Int): LocalDate {
        val thisMonth = today.withDayOfMonth(
            day.coerceAtMost(today.lengthOfMonth())
        )
        if (today.dayOfMonth < day.coerceAtMost(today.lengthOfMonth())) {
            return thisMonth
        }
        val nextMonth = today.plusMonths(1)
        return nextMonth.withDayOfMonth(
            day.coerceAtMost(nextMonth.lengthOfMonth())
        )
    }
}
