package com.hazard.koe.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hazard.koe.data.db.dao.AccountDao
import com.hazard.koe.data.db.dao.BudgetDao
import com.hazard.koe.data.db.dao.CasualLoanDao
import com.hazard.koe.data.db.dao.CasualLoanTransactionDao
import com.hazard.koe.data.db.dao.CategoryDao
import com.hazard.koe.data.db.dao.FormalLoanDao
import com.hazard.koe.data.db.dao.FormalLoanPaymentDao
import com.hazard.koe.data.db.dao.PersonDao
import com.hazard.koe.data.db.dao.RecurringRuleDao
import com.hazard.koe.data.db.dao.SubscriptionServiceDao
import com.hazard.koe.data.db.dao.ProcessedNotificationDao
import com.hazard.koe.data.db.dao.TransactionDao
import com.hazard.koe.data.db.dao.UserSubscriptionDao
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.Budget
import com.hazard.koe.data.model.CasualLoan
import com.hazard.koe.data.model.CasualLoanTransaction
import com.hazard.koe.data.model.Category
import com.hazard.koe.data.model.FormalLoan
import com.hazard.koe.data.model.FormalLoanPayment
import com.hazard.koe.data.model.Person
import com.hazard.koe.data.model.RecurringRule
import com.hazard.koe.data.model.SubscriptionService
import com.hazard.koe.data.model.ProcessedNotification
import com.hazard.koe.data.model.Transaction
import com.hazard.koe.data.model.UserSubscription

@Database(
    entities = [
        Account::class,
        Category::class,
        Transaction::class,
        RecurringRule::class,
        SubscriptionService::class,
        Budget::class,
        Person::class,
        CasualLoan::class,
        CasualLoanTransaction::class,
        FormalLoan::class,
        FormalLoanPayment::class,
        ProcessedNotification::class,
        UserSubscription::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class TrackerDatabase : RoomDatabase() {

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE transactions ADD COLUMN longitude REAL")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
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
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `processed_notifications` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `dedupKey` TEXT NOT NULL,
                        `operationNumber` TEXT,
                        `amount` INTEGER NOT NULL,
                        `type` TEXT NOT NULL,
                        `processedAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_processed_notifications_dedupKey` ON `processed_notifications` (`dedupKey`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_processed_notifications_operationNumber` ON `processed_notifications` (`operationNumber`)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_subscriptions ADD COLUMN iconResName TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_subscriptions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `serviceId` INTEGER,
                        `accountId` INTEGER NOT NULL,
                        `amount` INTEGER NOT NULL,
                        `billingDay` INTEGER NOT NULL,
                        `currencyCode` TEXT NOT NULL,
                        `customName` TEXT,
                        `customEmoji` TEXT,
                        `isArchived` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON DELETE RESTRICT
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_user_subscriptions_serviceId` ON `user_subscriptions` (`serviceId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_user_subscriptions_accountId` ON `user_subscriptions` (`accountId`)")
            }
        }
    }
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun recurringRuleDao(): RecurringRuleDao
    abstract fun subscriptionServiceDao(): SubscriptionServiceDao
    abstract fun budgetDao(): BudgetDao
    abstract fun personDao(): PersonDao
    abstract fun casualLoanDao(): CasualLoanDao
    abstract fun casualLoanTransactionDao(): CasualLoanTransactionDao
    abstract fun formalLoanDao(): FormalLoanDao
    abstract fun formalLoanPaymentDao(): FormalLoanPaymentDao
    abstract fun processedNotificationDao(): ProcessedNotificationDao
    abstract fun userSubscriptionDao(): UserSubscriptionDao
}
