package com.example.tracker.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TrackerDatabase::class.java
    )

    // Future migration tests go here.
    // Example structure for when version 2 is introduced:
    //
    // @Test
    // fun migrate1To2() {
    //     helper.createDatabase(TEST_DB, 1).apply { close() }
    //     helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
    // }
    //
    // companion object {
    //     private const val TEST_DB = "migration-test"
    // }
}
