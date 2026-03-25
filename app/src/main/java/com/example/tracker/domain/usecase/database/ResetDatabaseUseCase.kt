package com.example.tracker.domain.usecase.database

import com.example.tracker.data.db.DatabaseSeeder
import com.example.tracker.data.db.TrackerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResetDatabaseUseCase(private val database: TrackerDatabase) {

    suspend operator fun invoke() {
        withContext(Dispatchers.IO) {
            database.openHelper.writableDatabase.let { db ->
                DatabaseSeeder.resetAndReseed(db)
            }
        }
    }
}
