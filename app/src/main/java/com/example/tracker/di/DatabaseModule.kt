package com.example.tracker.di

import androidx.room.Room
import com.example.tracker.data.db.DatabaseSeeder
import com.example.tracker.data.db.TrackerDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            TrackerDatabase::class.java,
            "tracker_database"
        )
            .addCallback(DatabaseSeeder())
            .addMigrations(TrackerDatabase.MIGRATION_1_2, TrackerDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration(true)
            .build()
    }
    single { get<TrackerDatabase>().accountDao() }
    single { get<TrackerDatabase>().categoryDao() }
    single { get<TrackerDatabase>().transactionDao() }
    single { get<TrackerDatabase>().recurringRuleDao() }
    single { get<TrackerDatabase>().subscriptionServiceDao() }
    single { get<TrackerDatabase>().budgetDao() }
    single { get<TrackerDatabase>().personDao() }
    single { get<TrackerDatabase>().casualLoanDao() }
    single { get<TrackerDatabase>().casualLoanTransactionDao() }
    single { get<TrackerDatabase>().formalLoanDao() }
    single { get<TrackerDatabase>().formalLoanPaymentDao() }
    single { get<TrackerDatabase>().processedNotificationDao() }
}
