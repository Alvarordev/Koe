package com.hazard.koe.presentation.voice.voicetransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazard.koe.data.enums.CategoryType
import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.Category
import com.hazard.koe.data.model.Transaction
import com.hazard.koe.data.voice.VoiceAudioRecorder
import com.hazard.koe.domain.model.VoiceAccountContext
import com.hazard.koe.domain.model.VoiceCategoryContext
import com.hazard.koe.domain.model.VoiceTransactionInferenceRequest
import com.hazard.koe.domain.usecase.account.GetAccountsUseCase
import com.hazard.koe.domain.usecase.category.GetCategoriesUseCase
import com.hazard.koe.domain.usecase.transaction.CreateTransactionUseCase
import com.hazard.koe.domain.usecase.voice.InferTransactionFromVoiceUseCase
import com.hazard.koe.domain.usecase.voice.ObserveVoiceLocationSettingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceTransactionViewModel(
    private val getAccounts: GetAccountsUseCase,
    private val getCategories: GetCategoriesUseCase,
    private val createTransaction: CreateTransactionUseCase,
    private val inferTransactionFromVoice: InferTransactionFromVoiceUseCase,
    private val voiceAudioRecorder: VoiceAudioRecorder,
    private val observeVoiceLocationSetting: ObserveVoiceLocationSettingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceTransactionUiState())
    val uiState: StateFlow<VoiceTransactionUiState> = _uiState

    init {
        observeAccounts()
        observeCategories()
        observeLocationPreference()
    }

    fun startRecording() {
        val startResult = voiceAudioRecorder.startRecording()
        if (startResult.isFailure) {
            _uiState.update {
                it.copy(
                    phase = VoiceTransactionPhase.ERROR,
                    errorMessage = "No se pudo iniciar la grabación"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                phase = VoiceTransactionPhase.RECORDING,
                errorMessage = null,
                saveSuccess = false
            )
        }
    }

    fun stopRecording() {
        val recordedAudioResult = voiceAudioRecorder.stopRecording()
        recordedAudioResult
            .onSuccess { recordedAudio ->
                processRecordedAudio(recordedAudio.bytes, recordedAudio.mimeType)
            }
            .onFailure {
                _uiState.update {
                    it.copy(
                        phase = VoiceTransactionPhase.ERROR,
                        errorMessage = "No se pudo completar la grabación"
                    )
                }
            }
    }

    fun retry() {
        startRecording()
    }

    fun goManualEdit() {
        _uiState.update { it.copy(phase = VoiceTransactionPhase.MANUAL_EDIT, errorMessage = null) }
    }

    fun confirmAndSave() {
        val state = _uiState.value
        val account = state.selectedAccount
        val category = state.selectedCategory
        if (account == null || category == null || state.amountMinor <= 0L) {
            _uiState.update {
                it.copy(
                    phase = VoiceTransactionPhase.ERROR,
                    errorMessage = "Completa cuenta, categoría y monto antes de guardar"
                )
            }
            return
        }

        if (state.isLocationEnabled && (state.latitude == null || state.longitude == null)) {
            _uiState.update {
                it.copy(
                    phase = VoiceTransactionPhase.ERROR,
                    errorMessage = "No se pudo obtener ubicación. Verifica permisos o intenta de nuevo"
                )
            }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching {
                createTransaction(
                    Transaction(
                        type = state.transactionType,
                        amount = state.amountMinor,
                        description = state.description.ifBlank { null },
                        accountId = account.id,
                        categoryId = category.id,
                        date = state.selectedDate,
                        latitude = if (state.isLocationEnabled) state.latitude else null,
                        longitude = if (state.isLocationEnabled) state.longitude else null
                    )
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        phase = VoiceTransactionPhase.SUCCESS,
                        saveSuccess = true
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        phase = VoiceTransactionPhase.ERROR,
                        errorMessage = "No se pudo guardar la transacción"
                    )
                }
            }
        }
    }

    fun updateAmountFromInput(input: String) {
        val parsed = parseMinorUnits(input)
        _uiState.update { it.copy(amountMinor = parsed) }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun updateSelectedCategory(category: Category) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                transactionType = if (category.type == CategoryType.INCOME) TransactionType.INCOME else TransactionType.EXPENSE
            )
        }
    }

    fun updateSelectedAccount(account: Account) {
        _uiState.update { it.copy(selectedAccount = account) }
    }

    fun updateLocation(latitude: Double?, longitude: Double?) {
        _uiState.update { it.copy(latitude = latitude, longitude = longitude) }
    }

    fun consumeSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    private fun observeAccounts() {
        viewModelScope.launch {
            getAccounts().collect { accounts ->
                val activeAccounts = accounts.filter { !it.isArchived }
                _uiState.update {
                    it.copy(
                        accounts = accounts,
                        selectedAccount = it.selectedAccount ?: activeAccounts.minByOrNull { account -> account.sortOrder }
                    )
                }
            }
        }
    }

    private fun observeCategories() {
        viewModelScope.launch {
            getCategories().collect { categories ->
                _uiState.update {
                    it.copy(
                        categories = categories,
                        selectedCategory = it.selectedCategory ?: categories.firstOrNull()
                    )
                }
            }
        }
    }

    private fun observeLocationPreference() {
        viewModelScope.launch {
            observeVoiceLocationSetting().collect { enabled ->
                _uiState.update { state -> state.copy(isLocationEnabled = enabled) }
            }
        }
    }

    private fun processRecordedAudio(audioBytes: ByteArray, audioMimeType: String) {
        val state = _uiState.value
        _uiState.update { it.copy(phase = VoiceTransactionPhase.PROCESSING, errorMessage = null) }
        val request = VoiceTransactionInferenceRequest(
            audioBytes = audioBytes,
            audioMimeType = audioMimeType,
            localeTag = Locale.getDefault().toLanguageTag(),
            accounts = state.accounts.map {
                VoiceAccountContext(
                    id = it.id,
                    name = it.name,
                    currencyCode = it.currencyCode
                )
            },
            categories = state.categories.map {
                VoiceCategoryContext(
                    id = it.id,
                    name = it.name,
                    type = it.type
                )
            }
        )

        viewModelScope.launch {
            inferTransactionFromVoice(request)
                .onSuccess { inference ->
                    val account = state.accounts.firstOrNull { it.id == inference.accountId }
                    val category = state.categories.firstOrNull { it.id == inference.categoryId }
                        ?: state.categories.firstOrNull { categoryCandidate ->
                            categoryCandidate.type == if (inference.transactionType == TransactionType.INCOME) CategoryType.INCOME else CategoryType.EXPENSE
                        }

                    val nextPhase = if (inference.confidence >= state.confidenceThreshold) {
                        VoiceTransactionPhase.CONFIRM
                    } else {
                        VoiceTransactionPhase.MANUAL_EDIT
                    }

                    _uiState.update {
                        it.copy(
                            phase = nextPhase,
                            confidence = inference.confidence,
                            amountMinor = inference.amountMinor,
                            transactionType = inference.transactionType,
                            selectedAccount = account ?: it.selectedAccount,
                            selectedCategory = category ?: it.selectedCategory,
                            description = inference.description ?: it.description,
                            errorMessage = null
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            phase = VoiceTransactionPhase.ERROR,
                            errorMessage = "No se pudo interpretar el audio"
                        )
                    }
                }
        }
    }

    private fun parseMinorUnits(input: String): Long {
        val cleaned = input.trim().replace(',', '.')
        if (cleaned.isBlank()) return 0L
        val parts = cleaned.split('.')
        val integerPart = parts.firstOrNull()?.filter { it.isDigit() }.orEmpty().ifBlank { "0" }
        val fractionRaw = parts.getOrNull(1)?.filter { it.isDigit() }.orEmpty()
        val fraction = fractionRaw.padEnd(2, '0').take(2)
        val major = integerPart.toLongOrNull() ?: 0L
        val minor = fraction.toLongOrNull() ?: 0L
        return major * 100L + minor
    }

    override fun onCleared() {
        voiceAudioRecorder.release()
        super.onCleared()
    }
}
