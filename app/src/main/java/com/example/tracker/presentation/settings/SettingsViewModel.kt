package com.example.tracker.presentation.settings

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tracker.data.preferences.ThemePreferences
import com.example.tracker.data.preferences.YapePreferences
import com.example.tracker.domain.usecase.database.ResetDatabaseUseCase
import com.example.tracker.feature.yape.YapeNotificationListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themePreferences: ThemePreferences,
    private val yapePreferences: YapePreferences,
    private val resetDatabaseUseCase: ResetDatabaseUseCase,
    private val context: Context
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = themePreferences.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    private val _yapeActive = MutableStateFlow(false)
    val yapeActive: StateFlow<Boolean> = _yapeActive.asStateFlow()

    private val _isResetting = MutableStateFlow(false)
    val isResetting: StateFlow<Boolean> = _isResetting.asStateFlow()

    private val _resetComplete = MutableStateFlow(false)
    val resetComplete: StateFlow<Boolean> = _resetComplete.asStateFlow()

    val isOnboardingComplete: StateFlow<Boolean> = yapePreferences.isOnboardingComplete
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    fun refreshYapeStatus() {
        _yapeActive.value = isNotificationListenerEnabled(context)
    }

    fun toggleTheme() {
        viewModelScope.launch {
            themePreferences.setDarkMode(!isDarkMode.value)
        }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            _isResetting.value = true
            try {
                resetDatabaseUseCase()
                _resetComplete.value = true
            } finally {
                _isResetting.value = false
            }
        }
    }

    fun onResetCompleteHandled() {
        _resetComplete.value = false
    }

    companion object {
        fun isNotificationListenerEnabled(context: Context): Boolean {
            val componentName = ComponentName(context, YapeNotificationListenerService::class.java)
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: ""
            return flat.split(":").any { ComponentName.unflattenFromString(it) == componentName }
        }
    }
}
