package com.example.tracker.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tracker.data.enums.AccountType
import com.example.tracker.data.enums.CategoryType
import com.example.tracker.data.model.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseSeeder : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            seedAll(db)
        }
    }

    companion object {

        fun seedAll(db: SupportSQLiteDatabase) {
            seedCategories(db)
            seedDefaultAccount(db)
        }

        fun clearAllTables(db: SupportSQLiteDatabase) {
            db.execSQL("DELETE FROM transactions")
            db.execSQL("DELETE FROM recurring_rules")
            db.execSQL("DELETE FROM subscription_services")
            db.execSQL("DELETE FROM budgets")
            db.execSQL("DELETE FROM casual_loan_transactions")
            db.execSQL("DELETE FROM casual_loans")
            db.execSQL("DELETE FROM formal_loan_payments")
            db.execSQL("DELETE FROM formal_loans")
            db.execSQL("DELETE FROM persons")
            db.execSQL("DELETE FROM processed_notifications")
            db.execSQL("DELETE FROM categories")
            db.execSQL("DELETE FROM accounts")
        }

        fun resetAndReseed(db: SupportSQLiteDatabase) {
            clearAllTables(db)
            seedAll(db)
        }

        private fun seedCategories(db: SupportSQLiteDatabase) {
            val userCategories = listOf(
                Category(name = "Comida", emoji = "\uD83C\uDF54", color = "#FF5722", type = CategoryType.EXPENSE),
                Category(name = "Entretenimiento", emoji = "\uD83C\uDFAC", color = "#E91E63", type = CategoryType.EXPENSE),
                Category(name = "Transporte", emoji = "\uD83D\uDE97", color = "#2196F3", type = CategoryType.EXPENSE),
                Category(name = "Salud", emoji = "\uD83D\uDC8A", color = "#F44336", type = CategoryType.EXPENSE),
                Category(name = "Salario", emoji = "\uD83D\uDCBC", color = "#4CAF50", type = CategoryType.INCOME)
            )

            userCategories.forEachIndexed { index, category ->
                db.execSQL(
                    """INSERT OR IGNORE INTO categories (name, emoji, color, type, isSystem, isArchived, sortOrder, createdAt)
                       VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
                    arrayOf<Any?>(
                        category.name,
                        category.emoji,
                        category.color,
                        category.type.name,
                        0,
                        0,
                        index,
                        System.currentTimeMillis()
                    )
                )
            }

            db.execSQL(
                """INSERT OR IGNORE INTO categories (name, emoji, color, type, isSystem, isArchived, sortOrder, createdAt)
                   VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
                arrayOf<Any?>(
                    "Transfer",
                    "\uD83D\uDD04",
                    "#6B7280",
                    "EXPENSE",
                    1,
                    1,
                    999,
                    System.currentTimeMillis()
                )
            )
        }

        private fun seedDefaultAccount(db: SupportSQLiteDatabase) {
            db.execSQL(
                """INSERT OR IGNORE INTO accounts (name, type, color, currencyCode, initialBalance, currentBalance, sortOrder, isArchived, createdAt, updatedAt)
                   VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
                arrayOf<Any?>(
                    "Efectivo",
                    AccountType.CASH.name,
                    "#4CAF50",
                    "PEN",
                    0L,
                    0L,
                    0,
                    0,
                    System.currentTimeMillis(),
                    System.currentTimeMillis()
                )
            )
        }
    }
}
