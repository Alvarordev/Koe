package com.hazard.koe.presentation.settings

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazard.koe.data.preferences.ThemePreference
import com.hazard.koe.data.preferences.ThemePreferences
import com.hazard.koe.data.preferences.YapePreferences
import com.hazard.koe.domain.usecase.database.ExportDataUseCase
import com.hazard.koe.domain.usecase.database.ImportDataUseCase
import com.hazard.koe.domain.usecase.database.ImportMissingFileException
import com.hazard.koe.domain.usecase.database.ImportResult
import com.hazard.koe.domain.usecase.database.ResetDatabaseUseCase
import com.hazard.koe.feature.yape.YapeNotificationListenerService
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
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase,
    private val context: Context
) : ViewModel() {

    val themePreference: StateFlow<ThemePreference> = themePreferences.themePreference
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemePreference.System
        )

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

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportedFile = MutableStateFlow<java.io.File?>(null)
    val exportedFile: StateFlow<java.io.File?> = _exportedFile.asStateFlow()

    private val _exportError = MutableStateFlow<String?>(null)
    val exportError: StateFlow<String?> = _exportError.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importComplete = MutableStateFlow(false)
    val importComplete: StateFlow<Boolean> = _importComplete.asStateFlow()

    private val _importError = MutableStateFlow<String?>(null)
    val importError: StateFlow<String?> = _importError.asStateFlow()

    val isOnboardingComplete: StateFlow<Boolean> = yapePreferences.isOnboardingComplete
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    fun refreshYapeStatus() {
        _yapeActive.value = isNotificationListenerEnabled(context)
    }

    fun setThemePreference(preference: ThemePreference) {
        viewModelScope.launch {
            themePreferences.setThemePreference(preference)
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

    fun exportData() {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val file = exportDataUseCase(context.cacheDir)
                _exportedFile.value = file
            } catch (e: Exception) {
                _exportError.value = e.message ?: "Error al exportar datos"
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun onExportHandled() {
        _exportedFile.value = null
    }

    fun onExportErrorHandled() {
        _exportError.value = null
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("No se pudo abrir el archivo")
                importDataUseCase(inputStream)
                _importComplete.value = true
            } catch (e: ImportMissingFileException) {
                _importError.value = e.message
            } catch (e: Exception) {
                _importError.value = e.message ?: "Error al importar datos"
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun onImportCompleteHandled() {
        _importComplete.value = false
    }

    fun onImportErrorHandled() {
        _importError.value = null
    }

    companion object {
        fun isNotificationListenerEnabled(context: Context): Boolean {
            val componentName = ComponentName(context, YapeNotificationListenerService::class.java)
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: ""
            return flat.split(":").any { ComponentName.unflattenFromString(it) == componentName }
        }
    }
}
