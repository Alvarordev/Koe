package com.example.tracker.data.model.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.tracker.data.model.Account
import com.example.tracker.data.model.Category
import com.example.tracker.data.model.Transaction

data class TransactionWithDetails(
    @Embedded val transaction: Transaction,
    @Relation(parentColumn = "accountId", entityColumn = "id")
    val account: Account,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: Category,
    @Relation(parentColumn = "transferToAccountId", entityColumn = "id")
    val transferToAccounts: List<Account>
) {
    val transferToAccount: Account? get() = transferToAccounts.firstOrNull()
}
