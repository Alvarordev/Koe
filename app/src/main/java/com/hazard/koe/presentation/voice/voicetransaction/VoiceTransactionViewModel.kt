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
import com.hazard.koe.domain.usecase.transaction.DeleteTransactionUseCase
import com.hazard.koe.domain.usecase.voice.InferTransactionFromVoiceUseCase
import com.hazard.koe.domain.usecase.voice.ObserveVoiceLocationSettingUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceTransactionViewModel(
    private val getAccounts: GetAccountsUseCase,
    private val getCategories: GetCategoriesUseCase,
    private val createTransaction: CreateTransactionUseCase,
    private val deleteTransaction: DeleteTransactionUseCase,
    private val inferTransactionFromVoice: InferTransactionFromVoiceUseCase,
    private val voiceAudioRecorder: VoiceAudioRecorder,
    private val observeVoiceLocationSetting: ObserveVoiceLocationSettingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceTransactionUiState())
    val uiState: StateFlow<VoiceTransactionUiState> = _uiState
    private val _creationResult = MutableSharedFlow<VoiceTransactionCreationResult>(extraBufferCapacity = 1)
    val creationResult: SharedFlow<VoiceTransactionCreationResult> = _creationResult.asSharedFlow()
    private var recordingTimerJob: Job? = null
    private var lastCreatedTransaction: Transaction? = null

    init {
        observeAccounts()
        observeCategories()
        observeLocationPreference()
    }

    fun startRecording() {
        val startResult = voiceAudioRecorder.startRecording { rmsLevel ->
            _uiState.update { it.copy(rmsLevel = rmsLevel) }
        }
        if (startResult.isFailure) {
            stopTimer()
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
                recordingElapsedMillis = 0L,
                countdownSeconds = VoiceTransactionUiState.RECORDING_DURATION_SECONDS,
                rmsLevel = 0f
            )
        }
        startTimer()
    }

    fun stopRecording() {
        stopTimer()
        _uiState.update { it.copy(rmsLevel = 0f) }
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

    fun toggleRecording() {
        when (_uiState.value.phase) {
            VoiceTransactionPhase.RECORDING -> stopRecording()
            VoiceTransactionPhase.PROCESSING -> Unit
            else -> startRecording()
        }
    }

    fun undoLastCreatedTransaction() {
        val transaction = lastCreatedTransaction ?: return
        viewModelScope.launch {
            runCatching {
                deleteTransaction(transaction)
                lastCreatedTransaction = null
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        phase = VoiceTransactionPhase.ERROR,
                        errorMessage = "No se pudo deshacer la transacción"
                    )
                }
            }
        }
    }

    fun updateLocation(latitude: Double?, longitude: Double?) {
        _uiState.update { it.copy(latitude = latitude, longitude = longitude) }
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
        _uiState.update { it.copy(phase = VoiceTransactionPhase.PROCESSING, errorMessage = null) }
        val state = _uiState.value
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
                    val currentState = _uiState.value
                    val account = currentState.accounts.firstOrNull { it.id == inference.accountId }
                    val category = currentState.categories.firstOrNull { it.id == inference.categoryId }
                        ?: currentState.categories.firstOrNull { categoryCandidate ->
                            categoryCandidate.type == if (inference.transactionType == TransactionType.INCOME) {
                                CategoryType.INCOME
                            } else {
                                CategoryType.EXPENSE
                            }
                        }

                    if (inference.amountMinor <= 0L || account == null || category == null) {
                        _uiState.update {
                            it.copy(
                                phase = VoiceTransactionPhase.ERROR,
                                errorMessage = "No se pudo completar la transacción con el audio"
                            )
                        }
                        return@onSuccess
                    }

                    createFromInference(
                        amountMinor = inference.amountMinor,
                        account = account,
                        category = category,
                        description = inference.description.orEmpty(),
                        transactionType = inference.transactionType
                    )
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

    private fun createFromInference(
        amountMinor: Long,
        account: Account,
        category: Category,
        description: String,
        transactionType: TransactionType
    ) {
        val snapshot = _uiState.value
        if (snapshot.isLocationEnabled && (snapshot.latitude == null || snapshot.longitude == null)) {
            _uiState.update {
                it.copy(
                    phase = VoiceTransactionPhase.ERROR,
                    errorMessage = "No se pudo obtener ubicación. Verifica permisos o intenta de nuevo"
                )
            }
            return
        }

        val transaction = Transaction(
            type = transactionType,
            amount = amountMinor,
            description = description.ifBlank { null },
            accountId = account.id,
            categoryId = category.id,
            date = System.currentTimeMillis(),
            latitude = if (snapshot.isLocationEnabled) snapshot.latitude else null,
            longitude = if (snapshot.isLocationEnabled) snapshot.longitude else null
        )

        _uiState.update {
            it.copy(
                isSubmitting = true,
                inferredAmountMinor = amountMinor,
                inferredDescription = description,
                inferredTransactionType = transactionType,
                selectedAccount = account,
                selectedCategory = category,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            runCatching {
                createTransaction(transaction)
            }.onSuccess { createdId ->
                lastCreatedTransaction = transaction.copy(id = createdId)
                _uiState.update { state ->
                    state.copy(
                        phase = VoiceTransactionPhase.IDLE,
                        isSubmitting = false,
                        recordingElapsedMillis = 0L,
                        countdownSeconds = VoiceTransactionUiState.RECORDING_DURATION_SECONDS
                    )
                }
                val amountDisplay = "S/ %.2f".format(amountMinor / 100.0)
                val label = if (description.isNotBlank()) {
                    "$description · $amountDisplay"
                } else {
                    "${category.name} · $amountDisplay"
                }
                _creationResult.emit(
                    VoiceTransactionCreationResult(
                        transactionId = createdId,
                        message = label,
                        undoLabel = "Deshacer"
                    )
                )
            }.onFailure {
                _uiState.update { state ->
                    state.copy(
                        phase = VoiceTransactionPhase.ERROR,
                        isSubmitting = false,
                        errorMessage = "No se pudo guardar la transacción"
                    )
                }
            }
        }
    }

    private fun startTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.update { state ->
                    if (state.phase == VoiceTransactionPhase.RECORDING) {
                        val newElapsed = state.recordingElapsedMillis + 1000L
                        val newCountdown = (VoiceTransactionUiState.RECORDING_DURATION_SECONDS - (newElapsed / 1000).toInt())
                            .coerceAtLeast(0)
                        state.copy(
                            recordingElapsedMillis = newElapsed,
                            countdownSeconds = newCountdown
                        )
                    } else {
                        state
                    }
                }
                if (_uiState.value.countdownSeconds <= 0) {
                    stopRecording()
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
    }

    override fun onCleared() {
        stopTimer()
        voiceAudioRecorder.release()
        super.onCleared()
    }
}
