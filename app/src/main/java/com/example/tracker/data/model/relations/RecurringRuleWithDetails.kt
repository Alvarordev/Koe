package com.example.tracker.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.tracker.data.model.Account
import com.example.tracker.data.model.Category
import com.example.tracker.data.model.RecurringRule
import com.example.tracker.data.model.SubscriptionService

data class RecurringRuleWithDetails(
    @Embedded val rule: RecurringRule,
    @Relation(parentColumn = "accountId", entityColumn = "id")
    val accounts: List<Account>,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val categories: List<Category>,
    @Relation(parentColumn = "subscriptionServiceId", entityColumn = "id")
    val subscriptionServices: List<SubscriptionService>
) {
    val account: Account? get() = accounts.firstOrNull()
    val category: Category? get() = categories.firstOrNull()
    val subscriptionService: SubscriptionService? get() = subscriptionServices.firstOrNull()
}
