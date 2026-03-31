package com.example.tracker.domain.usecase.subscription

import com.example.tracker.data.enums.TransactionType
import com.example.tracker.data.model.Transaction
import com.example.tracker.data.model.relations.SubscriptionWithDetails
import com.example.tracker.domain.repository.CategoryRepository
import com.example.tracker.domain.repository.TransactionRepository
import com.example.tracker.domain.repository.UserSubscriptionRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class ProcessSubscriptionBillingUseCase(
    private val subscriptionRepository: UserSubscriptionRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(subscriptionId: Long? = null, billCurrentMonth: Boolean = false) {
        val subscriptions = subscriptionRepository.getActive().first()
        val toProcess = if (subscriptionId != null) {
            subscriptions.filter { it.subscription.id == subscriptionId }
        } else {
            subscriptions
        }

        if (toProcess.isEmpty()) return

        val category = categoryRepository.getOrCreateSubscriptionCategory()
        val today = LocalDate.now(ZoneId.systemDefault())

        for (item in toProcess) {
            if (item.account == null) continue
            processSubscription(item, category.id, today, billCurrentMonth)
        }
    }

    private suspend fun processSubscription(
        item: SubscriptionWithDetails,
        categoryId: Long,
        today: LocalDate,
        billCurrentMonth: Boolean = false
    ) {
        val sub = item.subscription
        val zone = ZoneId.systemDefault()
        val displayName = sub.customName ?: item.service?.name ?: "Suscripción"

        val lastTransaction = transactionRepository.getLastBySubscriptionId(sub.id)

        val startYearMonth: YearMonth = if (lastTransaction == null) {
            val todayYearMonth = YearMonth.from(today)
            val billingDayPassedThisMonth = sub.billingDay <= today.dayOfMonth
            if (billingDayPassedThisMonth && !billCurrentMonth) {
                todayYearMonth.plusMonths(1)
            } else {
                todayYearMonth
            }
        } else {
            val lastDate = Instant.ofEpochMilli(lastTransaction.date).atZone(zone).toLocalDate()
            YearMonth.from(lastDate).plusMonths(1)
        }

        var currentYearMonth = startYearMonth
        while (true) {
            val billingDate = billingDateForMonth(currentYearMonth, sub.billingDay)
            if (billingDate.isAfter(today)) break

            val billingMillis = billingDate.atStartOfDay(zone).toInstant().toEpochMilli()
            transactionRepository.create(
                Transaction(
                    type = TransactionType.EXPENSE,
                    amount = sub.amount,
                    description = displayName,
                    accountId = sub.accountId,
                    categoryId = categoryId,
                    subscriptionId = sub.id,
                    date = billingMillis
                )
            )
            currentYearMonth = currentYearMonth.plusMonths(1)
        }
    }

    private fun billingDateForMonth(yearMonth: YearMonth, day: Int): LocalDate =
        yearMonth.atDay(minOf(day, yearMonth.lengthOfMonth()))
}
