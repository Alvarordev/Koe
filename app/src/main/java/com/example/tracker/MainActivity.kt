package com.example.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.example.tracker.presentation.navigation.TrackerScaffold
import com.example.tracker.presentation.settings.SettingsViewModel
import com.example.tracker.ui.theme.TrackerTheme
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        EmojiManager.install(IosEmojiProvider())

        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val isDarkMode by settingsViewModel.isDarkMode.collectAsStateWithLifecycle()
            TrackerTheme(darkTheme = isDarkMode) {
                TrackerScaffold()
            }
        }
    }
}
