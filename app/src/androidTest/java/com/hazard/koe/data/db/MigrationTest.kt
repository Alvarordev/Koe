package com.hazard.koe.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TrackerDatabase::class.java
    )

    @Test
    fun migrate6To7_addsClosingDayColumn() {
        helper.createDatabase(TEST_DB, 6).apply {
            execSQL(
                """
                INSERT INTO accounts (
                    id, name, type, color, currencyCode, initialBalance, currentBalance,
                    cardNetwork, lastFourDigits, expirationDate, creditLimit, creditUsed,
                    paymentDay, interestRate, sortOrder, isArchived, createdAt, updatedAt
                ) VALUES (
                    1, 'Credit', 'CREDIT', '#1A73E8', 'USD', 0, 0,
                    'VISA', NULL, NULL, 100000, 20000,
                    20, NULL, 0, 0, 1, 1
                )
                """.trimIndent()
            )
            close()
        }

        helper.runMigrationsAndValidate(
            TEST_DB,
            7,
            true,
            TrackerDatabase.MIGRATION_6_7
        )
    }

    companion object {
        private const val TEST_DB = "migration-test"
    }
}
