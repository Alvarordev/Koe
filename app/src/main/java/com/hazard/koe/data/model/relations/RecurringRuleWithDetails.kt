package com.hazard.koe.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.Category
import com.hazard.koe.data.model.RecurringRule
import com.hazard.koe.data.model.SubscriptionService

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
