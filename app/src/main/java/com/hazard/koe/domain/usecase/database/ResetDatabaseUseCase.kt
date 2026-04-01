package com.hazard.koe.domain.usecase.database

import com.hazard.koe.data.db.DatabaseSeeder
import com.hazard.koe.data.db.TrackerDatabase
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
