package com.hazard.koe.di

import com.hazard.koe.data.preferences.ExchangeRatePreferences
import com.hazard.koe.data.preferences.ThemePreferences
import com.hazard.koe.data.preferences.YapePreferences
import com.hazard.koe.data.ocr.YapeImageOcrProcessor
import com.hazard.koe.data.voice.AndroidVoiceAudioRecorder
import com.hazard.koe.data.voice.VoiceAudioRecorder
import com.hazard.koe.data.voice.VoiceTransactionParser
import com.hazard.koe.feature.yape.YapeNotificationParser
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val preferencesModule = module {
    single { ThemePreferences(androidContext()) }
    single { YapePreferences(androidContext()) }
    single { YapeNotificationParser() }
    single { YapeImageOcrProcessor(androidContext()) }
    single { ExchangeRatePreferences(androidContext()) }
    factory<VoiceAudioRecorder> { AndroidVoiceAudioRecorder(androidContext()) }
    single { VoiceTransactionParser() }
}
