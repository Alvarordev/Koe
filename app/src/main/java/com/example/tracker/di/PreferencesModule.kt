package com.example.tracker.di

import com.example.tracker.data.preferences.ThemePreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val preferencesModule = module {
    single { ThemePreferences(androidContext()) }
}
