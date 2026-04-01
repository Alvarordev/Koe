package com.hazard.koe.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.Category
import com.hazard.koe.data.model.Transaction
import com.hazard.koe.data.model.UserSubscription

data class TransactionWithDetails(
    @Embedded val transaction: Transaction,
    @Relation(parentColumn = "accountId", entityColumn = "id")
    val account: Account,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: Category,
    @Relation(parentColumn = "transferToAccountId", entityColumn = "id")
    val transferToAccounts: List<Account>,
    @Relation(parentColumn = "subscriptionId", entityColumn = "id")
    val subscriptions: List<UserSubscription>
) {
    val transferToAccount: Account? get() = transferToAccounts.firstOrNull()
    val subscription: UserSubscription? get() = subscriptions.firstOrNull()
}
