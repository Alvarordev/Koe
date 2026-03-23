package com.example.tracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.content.IntentCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tracker.presentation.navigation.TrackerScaffold
import com.example.tracker.presentation.settings.SettingsViewModel
import com.example.tracker.ui.theme.TrackerTheme
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.ios.IosEmojiProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val _sharedImageUri = MutableStateFlow<Uri?>(null)
    val sharedImageUri: StateFlow<Uri?> = _sharedImageUri.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleShareIntent(intent)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            val uri = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
            if (uri != null) {
                _sharedImageUri.value = uri
            }
        }
    }

    fun clearSharedImage() {
        _sharedImageUri.value = null
    }
}
