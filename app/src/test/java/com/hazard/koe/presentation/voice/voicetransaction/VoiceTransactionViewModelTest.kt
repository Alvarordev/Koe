package com.hazard.koe.presentation.voice.voicetransaction

import com.hazard.koe.data.enums.CategoryType
import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.Category
import com.hazard.koe.data.voice.RecordedVoiceAudio
import com.hazard.koe.data.voice.VoiceAudioRecorder
import com.hazard.koe.domain.model.VoiceTransactionInferenceRequest
import com.hazard.koe.domain.model.VoiceTransactionInferenceResult
import com.hazard.koe.domain.repository.AccountRepository
import com.hazard.koe.domain.repository.CategoryRepository
import com.hazard.koe.domain.repository.TransactionRepository
import com.hazard.koe.domain.repository.VoiceTransactionInferenceRepository
import com.hazard.koe.domain.repository.VoiceTransactionSettingsRepository
import com.hazard.koe.domain.usecase.account.GetAccountsUseCase
import com.hazard.koe.domain.usecase.category.GetCategoriesUseCase
import com.hazard.koe.domain.usecase.transaction.CreateTransactionUseCase
import com.hazard.koe.domain.usecase.voice.InferTransactionFromVoiceUseCase
import com.hazard.koe.domain.usecase.voice.ObserveVoiceLocationSettingUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceTransactionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun highConfidenceRouting_goesToConfirm() = runTest(testDispatcher) {
        val recorder = FakeVoiceAudioRecorder()
        val viewModel = createViewModel(
            voiceAudioRecorder = recorder,
            inferenceResult = Result.success(
                VoiceTransactionInferenceResult(
                    amountMinor = 2550L,
                    transactionType = TransactionType.EXPENSE,
                    categoryId = 10L,
                    accountId = 1L,
                    description = "Comida",
                    confidence = 0.92f,
                    reasoning = "high confidence"
                )
            )
        )

        recorder.recordedAudio = RecordedVoiceAudio(
            bytes = byteArrayOf(1, 2, 3),
            mimeType = "audio/mp4"
        )
        viewModel.startRecording()
        viewModel.stopRecording()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(VoiceTransactionPhase.CONFIRM, viewModel.uiState.value.phase)
    }

    @Test
    fun lowConfidenceRouting_goesToManualEdit() = runTest(testDispatcher) {
        val recorder = FakeVoiceAudioRecorder()
        val viewModel = createViewModel(
            voiceAudioRecorder = recorder,
            inferenceResult = Result.success(
                VoiceTransactionInferenceResult(
                    amountMinor = 2550L,
                    transactionType = TransactionType.EXPENSE,
                    categoryId = null,
                    accountId = null,
                    description = "Comida",
                    confidence = 0.35f,
                    reasoning = "uncertain"
                )
            )
        )

        recorder.recordedAudio = RecordedVoiceAudio(
            bytes = byteArrayOf(4, 5, 6),
            mimeType = "audio/mp4"
        )
        viewModel.startRecording()
        viewModel.stopRecording()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(VoiceTransactionPhase.MANUAL_EDIT, viewModel.uiState.value.phase)
    }

    @Test
    fun inferenceFailure_goesToError() = runTest(testDispatcher) {
        val recorder = FakeVoiceAudioRecorder()
        val viewModel = createViewModel(
            voiceAudioRecorder = recorder,
            inferenceResult = Result.failure(IllegalStateException("boom"))
        )

        recorder.recordedAudio = RecordedVoiceAudio(
            bytes = byteArrayOf(7, 8),
            mimeType = "audio/mp4"
        )
        viewModel.startRecording()
        viewModel.stopRecording()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(VoiceTransactionPhase.ERROR, viewModel.uiState.value.phase)
    }

    @Test
    fun recordingFailure_goesToError() = runTest(testDispatcher) {
        val recorder = FakeVoiceAudioRecorder().apply {
            stopFailure = IllegalStateException("stop failed")
        }
        val viewModel = createViewModel(
            voiceAudioRecorder = recorder,
            inferenceResult = Result.success(
                VoiceTransactionInferenceResult(
                    amountMinor = 100L,
                    transactionType = TransactionType.EXPENSE,
                    categoryId = 10L,
                    accountId = 1L,
                    description = "Prueba",
                    confidence = 0.9f,
                    reasoning = null
                )
            )
        )

        viewModel.startRecording()
        viewModel.stopRecording()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(VoiceTransactionPhase.ERROR, viewModel.uiState.value.phase)
    }

    private fun createViewModel(
        voiceAudioRecorder: FakeVoiceAudioRecorder,
        inferenceResult: Result<VoiceTransactionInferenceResult>
    ): VoiceTransactionViewModel {
        val account = Account(
            id = 1L,
            name = "BCP",
            type = com.hazard.koe.data.enums.AccountType.CASH,
            color = "#111111",
            currencyCode = "PEN",
            initialBalance = 0L,
            currentBalance = 0L,
            sortOrder = 0
        )
        val category = Category(
            id = 10L,
            name = "Comida",
            emoji = "🍔",
            color = "#FF0000",
            type = CategoryType.EXPENSE,
            sortOrder = 0
        )

        val accountRepo = object : AccountRepository {
            override fun getAll(): Flow<List<Account>> = flowOf(listOf(account))
            override fun getById(id: Long): Flow<Account?> = flowOf(null)
            override fun getByType(type: com.hazard.koe.data.enums.AccountType): Flow<List<Account>> = flowOf(emptyList())
            override fun getTotalBalance(): Flow<Long> = flowOf(0L)
            override fun getTotalBalanceByCurrency(): Flow<List<com.hazard.koe.data.model.relations.CurrencyBalance>> = flowOf(emptyList())
            override suspend fun create(account: Account): Long = 0L
            override suspend fun update(account: Account) = Unit
            override suspend fun updateBalance(id: Long, newBalance: Long) = Unit
            override suspend fun updateCreditUsed(id: Long, creditUsed: Long) = Unit
            override suspend fun archive(id: Long) = Unit
        }

        val categoryRepo = object : CategoryRepository {
            override fun getAll(): Flow<List<Category>> = flowOf(listOf(category))
            override fun getByType(type: CategoryType): Flow<List<Category>> = flowOf(emptyList())
            override fun getById(id: Long): Flow<Category?> = flowOf(null)
            override fun getSystemCategories(): Flow<List<Category>> = flowOf(emptyList())
            override suspend fun create(category: Category): Long = 0L
            override suspend fun update(category: Category) = Unit
            override suspend fun archive(id: Long) = Unit
            override suspend fun seedSystemCategories() = Unit
            override suspend fun getTransferCategory(): Category? = null
            override suspend fun getOrCreateSubscriptionCategory(): Category = category
        }

        val transactionRepo = object : TransactionRepository {
            override fun getAll() = flowOf(emptyList<com.hazard.koe.data.model.relations.TransactionWithDetails>())
            override fun getById(id: Long) = flowOf<com.hazard.koe.data.model.relations.TransactionWithDetails?>(null)
            override fun getByAccount(accountId: Long) = flowOf(emptyList<com.hazard.koe.data.model.relations.TransactionWithDetails>())
            override fun getByCategory(categoryId: Long) = flowOf(emptyList<com.hazard.koe.data.model.relations.TransactionWithDetails>())
            override fun getByDateRange(start: Long, end: Long) = flowOf(emptyList<com.hazard.koe.data.model.relations.TransactionWithDetails>())
            override fun getCategorySummaryInPeriod(categoryId: Long, start: Long, end: Long) = flowOf(com.hazard.koe.data.model.relations.CategorySummary(0, 0L))
            override fun getAllCategorySummariesInPeriod(start: Long, end: Long) = flowOf(emptyList<com.hazard.koe.data.model.relations.CategoryIdSummary>())
            override fun getExpensesByCategoryInPeriod(start: Long, end: Long) = flowOf(emptyList<com.hazard.koe.data.model.relations.CategoryTotal>())
            override fun getTotalByTypeInPeriod(type: TransactionType, start: Long, end: Long) = flowOf(0L)
            override suspend fun create(transaction: com.hazard.koe.data.model.Transaction): Long = 1L
            override suspend fun update(transaction: com.hazard.koe.data.model.Transaction) = Unit
            override suspend fun delete(transaction: com.hazard.koe.data.model.Transaction) = Unit
            override suspend fun getLastBySubscriptionId(subscriptionId: Long): com.hazard.koe.data.model.Transaction? = null
            override suspend fun deleteFutureBySubscriptionId(subscriptionId: Long, afterDate: Long) = Unit
        }

        val inferenceRepo = object : VoiceTransactionInferenceRepository {
            override suspend fun infer(request: VoiceTransactionInferenceRequest): Result<VoiceTransactionInferenceResult> = inferenceResult
        }

        val settingsRepo = object : VoiceTransactionSettingsRepository {
            override val isLocationEnabled: Flow<Boolean> = flowOf(false)
        }

        return VoiceTransactionViewModel(
            getAccounts = GetAccountsUseCase(accountRepo),
            getCategories = GetCategoriesUseCase(categoryRepo),
            createTransaction = CreateTransactionUseCase(transactionRepo),
            inferTransactionFromVoice = InferTransactionFromVoiceUseCase(inferenceRepo),
            voiceAudioRecorder = voiceAudioRecorder,
            observeVoiceLocationSetting = ObserveVoiceLocationSettingUseCase(settingsRepo)
        )
    }
}

private class FakeVoiceAudioRecorder : VoiceAudioRecorder {
    var recordedAudio: RecordedVoiceAudio = RecordedVoiceAudio(byteArrayOf(), "audio/mp4")
    var startFailure: Throwable? = null
    var stopFailure: Throwable? = null

    override fun startRecording(): Result<Unit> {
        val failure = startFailure
        return if (failure != null) Result.failure(failure) else Result.success(Unit)
    }

    override fun stopRecording(): Result<RecordedVoiceAudio> {
        val failure = stopFailure
        return if (failure != null) Result.failure(failure) else Result.success(recordedAudio)
    }

    override fun cancelRecording() = Unit

    override fun release() = Unit
}
