package com.example.tracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
        FormalLoanPayment::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class TrackerDatabase : RoomDatabase() {
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
}
