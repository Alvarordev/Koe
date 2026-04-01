package com.hazard.koe

import android.app.Application
import com.hazard.koe.di.databaseModule
import com.hazard.koe.di.preferencesModule
import com.hazard.koe.di.repositoryModule
import com.hazard.koe.di.useCaseModule
import com.hazard.koe.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class KoeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@KoeApp)
            modules(databaseModule, repositoryModule, useCaseModule, viewModelModule, preferencesModule)
        }
    }
}
