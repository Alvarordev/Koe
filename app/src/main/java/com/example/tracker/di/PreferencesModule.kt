package com.example.tracker.di

import com.example.tracker.data.preferences.ThemePreferences
import com.example.tracker.data.preferences.YapePreferences
import com.example.tracker.data.ocr.YapeImageOcrProcessor
import com.example.tracker.feature.yape.YapeNotificationParser
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val preferencesModule = module {
    single { ThemePreferences(androidContext()) }
    single { YapePreferences(androidContext()) }
    single { YapeNotificationParser() }
    single { YapeImageOcrProcessor(androidContext()) }
}
