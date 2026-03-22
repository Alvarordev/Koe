package com.example.tracker

import android.app.Application
import com.example.tracker.di.databaseModule
import com.example.tracker.di.repositoryModule
import com.example.tracker.di.useCaseModule
import com.example.tracker.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@TrackerApp)
            modules(databaseModule, repositoryModule, useCaseModule, viewModelModule)
        }
    }
}
