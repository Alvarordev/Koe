package com.example.tracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tracker.data.db.dao.AccountDao
import com.example.tracker.data.db.dao.BudgetDao
import com.example.tracker.data.db.dao.CasualLoanDao
import com.example.tracker.data.db.dao.CasualLoanTransactionDao
import com.example.tracker.data.db.dao.CategoryDao
import com.example.tracker.data.db.dao.FormalLoanDao
import com.example.tracker.data.db.dao.FormalLoanPaymentDao
import com.example.tracker.data.db.dao.PersonDao
import com.example.tracker.data.db.dao.RecurringRuleDao
import com.example.tracker.data.db.dao.SubscriptionServiceDao
import com.example.tracker.data.db.dao.ProcessedNotificationDao
import com.example.tracker.data.db.dao.TransactionDao
import com.example.tracker.data.model.Account
import com.example.tracker.data.model.Budget
import com.example.tracker.data.model.CasualLoan
import com.example.tracker.data.model.CasualLoanTransaction
import com.example.tracker.data.model.Category
import com.example.tracker.data.model.FormalLoan
import com.example.tracker.data.model.FormalLoanPayment
import com.example.tracker.data.model.Person
import com.example.tracker.data.model.RecurringRule
import com.example.tracker.data.model.SubscriptionService
import com.example.tracker.data.model.ProcessedNotification
import com.example.tracker.data.model.Transaction

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
        ProcessedNotification::class
    ],
    version = 3,
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
}
