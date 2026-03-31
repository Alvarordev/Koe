package com.example.tracker.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.tracker.data.model.Account
import com.example.tracker.data.model.SubscriptionService
import com.example.tracker.data.model.UserSubscription

data class SubscriptionWithDetails(
    @Embedded val subscription: UserSubscription,
    @Relation(parentColumn = "serviceId", entityColumn = "id")
    val services: List<SubscriptionService>,
    @Relation(parentColumn = "accountId", entityColumn = "id")
    val accounts: List<Account>
) {
    val service: SubscriptionService? get() = services.firstOrNull()
    val account: Account? get() = accounts.firstOrNull()
}
