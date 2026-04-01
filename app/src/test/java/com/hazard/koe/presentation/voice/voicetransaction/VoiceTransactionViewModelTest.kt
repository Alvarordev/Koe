package com.hazard.koe.presentation.voice.voicetransaction

import com.hazard.koe.data.enums.AccountType
import com.hazard.koe.data.enums.CategoryType
import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.Category
import com.hazard.koe.data.model.Transaction
import com.hazard.koe.data.model.relations.CategoryIdSummary
import com.hazard.koe.data.model.relations.CategorySummary
import com.hazard.koe.data.model.relations.CategoryTotal
import com.hazard.koe.data.model.relations.CurrencyBalance
import com.hazard.koe.data.model.relations.TransactionWithDetails
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
import com.hazard.koe.domain.usecase.transaction.DeleteTransactionUseCase
import com.hazard.koe.domain.usecase.voice.InferTransactionFromVoiceUseCase
import com.hazard.koe.domain.usecase.voice.ObserveVoiceLocationSettingUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
    fun successfulInference_createsImmediately_andEmitsCreationEvent() = runTest(testDispatcher) {
        val recorder = FakeVoiceAudioRecorder().apply {
            recordedAudio = RecordedVoiceAudio(byteArrayOf(1, 2, 3), "audio/mp4")
        }
        val transactionRepo = FakeTransactionRepository()
        val viewModel = createViewModel(
            voiceAudioRecorder = recorder,
            transactionRepository = transactionRepo,
            inferenceResult = Result.success(
                VoiceTransactionInferenceResult(
                    amountMinor = 2550L,
                    transactionType = TransactionType.EXPENSE,
                    categoryId = 10L,
                    accountId = 1L,
                    description = "Comida",
                    confidence = 0.95f,
                    reasoning = "clear"
                )
            ),
            locationEnabled = false
        )

        val eventDeferred = async { viewModel.creationResult.first() }

        viewModel.startRecording()
        viewModel.stopRecording()
        advanceUntilIdle()

        val event = eventDeferred.await()
        assertEquals(1, transactionRepo.createdTransactions.size)
        assertEquals(1L, event.transactionId)
        assertEquals("Transacción creada", event.message)
        assertEquals("Deshacer", event.undoLabel)
        assertEquals(VoiceTransactionPhase.IDLE, viewModel.uiState.value.phase)
    }

    @Test
    fun undoAfterCreation_deletesCreatedTransaction() = runTest(testDispatcher) {
        val recorder = FakeVoiceAudioRecorder().apply {
            recordedAudio = RecordedVoiceAudio(byteArrayOf(1), "audio/mp4")
        }
        val transactionRepo = FakeTransactionRepository()
        val viewModel = createViewModel(
            voiceAudioRecorder = recorder,
            transactionRepository = transactionRepo,
            inferenceResult = Result.success(
                VoiceTransactionInferenceResult(
                    amountMinor = 900L,
                    transactionType = TransactionType.EXPENSE,
                    categoryId = 10L,
                    accountId = 1L,
                    description = "Taxi",
                    confidence = 0.9f,
                    reasoning = null
                )
            ),
            locationEnabled = false
        )

        viewModel.startRecording()
        viewModel.stopRecording()
        advanceUntilIdle()

        viewModel.undoLastCreatedTransaction()
        advanceUntilIdle()

        assertEquals(1, transactionRepo.deletedTransactions.size)
        assertEquals(1L, transactionRepo.deletedTransactions.first().id)
    }

    @Test
    fun missingRequiredFields_keepsErrorState_andDoesNotCreate() = runTest(testDispatcher) {
        val recorder = FakeVoiceAudioRecorder().apply {
            recordedAudio = RecordedVoiceAudio(byteArrayOf(7, 8), "audio/mp4")
        }
        val transactionRepo = FakeTransactionRepository()
        val viewModel = createViewModel(
            voiceAudioRecorder = recorder,
            transactionRepository = transactionRepo,
            inferenceResult = Result.success(
                VoiceTransactionInferenceResult(
                    amountMinor = 1200L,
                    transactionType = TransactionType.EXPENSE,
                    categoryId = null,
                    accountId = null,
                    description = "Incierto",
                    confidence = 0.2f,
                    reasoning = "missing ids"
                )
            ),
            locationEnabled = false
        )

        viewModel.startRecording()
        viewModel.stopRecording()
        advanceUntilIdle()

        assertEquals(0, transactionRepo.createdTransactions.size)
        assertEquals(VoiceTransactionPhase.ERROR, viewModel.uiState.value.phase)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun inferenceFailure_goesToError() = runTest(testDispatcher) {
        val recorder = FakeVoiceAudioRecorder().apply {
            recordedAudio = RecordedVoiceAudio(byteArrayOf(9), "audio/mp4")
        }
        val transactionRepo = FakeTransactionRepository()
        val viewModel = createViewModel(
            voiceAudioRecorder = recorder,
            transactionRepository = transactionRepo,
            inferenceResult = Result.failure(IllegalStateException("boom")),
            locationEnabled = false
        )

        viewModel.startRecording()
        viewModel.stopRecording()
        advanceUntilIdle()

        assertEquals(0, transactionRepo.createdTransactions.size)
        assertEquals(VoiceTransactionPhase.ERROR, viewModel.uiState.value.phase)
        assertTrue(viewModel.uiState.value.errorMessage?.isNotBlank() == true)
    }

    @Test
    fun locationEnabledWithoutCoordinates_keepsErrorState() = runTest(testDispatcher) {
        val recorder = FakeVoiceAudioRecorder().apply {
            recordedAudio = RecordedVoiceAudio(byteArrayOf(4, 5), "audio/mp4")
        }
        val transactionRepo = FakeTransactionRepository()
        val viewModel = createViewModel(
            voiceAudioRecorder = recorder,
            transactionRepository = transactionRepo,
            inferenceResult = Result.success(
                VoiceTransactionInferenceResult(
                    amountMinor = 1800L,
                    transactionType = TransactionType.EXPENSE,
                    categoryId = 10L,
                    accountId = 1L,
                    description = "Cena",
                    confidence = 0.9f,
                    reasoning = null
                )
            ),
            locationEnabled = true
        )

        viewModel.startRecording()
        viewModel.stopRecording()
        advanceUntilIdle()

        assertEquals(0, transactionRepo.createdTransactions.size)
        assertEquals(VoiceTransactionPhase.ERROR, viewModel.uiState.value.phase)
    }

    private fun createViewModel(
        voiceAudioRecorder: FakeVoiceAudioRecorder,
        transactionRepository: FakeTransactionRepository,
        inferenceResult: Result<VoiceTransactionInferenceResult>,
        locationEnabled: Boolean
    ): VoiceTransactionViewModel {
        val account = Account(
            id = 1L,
            name = "BCP",
            type = AccountType.CASH,
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
            override fun getByType(type: AccountType): Flow<List<Account>> = flowOf(emptyList())
            override fun getTotalBalance(): Flow<Long> = flowOf(0L)
            override fun getTotalBalanceByCurrency(): Flow<List<CurrencyBalance>> = flowOf(emptyList())
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

        val inferenceRepo = object : VoiceTransactionInferenceRepository {
            override suspend fun infer(request: VoiceTransactionInferenceRequest): Result<VoiceTransactionInferenceResult> {
                return inferenceResult
            }
        }

        val settingsRepo = object : VoiceTransactionSettingsRepository {
            override val isLocationEnabled: Flow<Boolean> = flowOf(locationEnabled)
        }

        return VoiceTransactionViewModel(
            getAccounts = GetAccountsUseCase(accountRepo),
            getCategories = GetCategoriesUseCase(categoryRepo),
            createTransaction = CreateTransactionUseCase(transactionRepository),
            deleteTransaction = DeleteTransactionUseCase(transactionRepository),
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

    override fun startRecording(onRmsChanged: ((Float) -> Unit)?): Result<Unit> {
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

private class FakeTransactionRepository : TransactionRepository {
    val createdTransactions = mutableListOf<Transaction>()
    val deletedTransactions = mutableListOf<Transaction>()
    private var idCounter = 1L

    override fun getAll(): Flow<List<TransactionWithDetails>> = flowOf(emptyList())

    override fun getById(id: Long): Flow<TransactionWithDetails?> = flowOf(null)

    override fun getByAccount(accountId: Long): Flow<List<TransactionWithDetails>> = flowOf(emptyList())

    override fun getByCategory(categoryId: Long): Flow<List<TransactionWithDetails>> = flowOf(emptyList())

    override fun getByDateRange(start: Long, end: Long): Flow<List<TransactionWithDetails>> = flowOf(emptyList())

    override fun getCategorySummaryInPeriod(
        categoryId: Long,
        start: Long,
        end: Long
    ): Flow<CategorySummary> = flowOf(CategorySummary(0, 0L))

    override fun getAllCategorySummariesInPeriod(start: Long, end: Long): Flow<List<CategoryIdSummary>> = flowOf(emptyList())

    override fun getExpensesByCategoryInPeriod(start: Long, end: Long): Flow<List<CategoryTotal>> = flowOf(emptyList())

    override fun getTotalByTypeInPeriod(type: TransactionType, start: Long, end: Long): Flow<Long> = flowOf(0L)

    override suspend fun create(transaction: Transaction): Long {
        createdTransactions += transaction
        return idCounter++
    }

    override suspend fun update(transaction: Transaction) = Unit

    override suspend fun delete(transaction: Transaction) {
        deletedTransactions += transaction
    }

    override suspend fun getLastBySubscriptionId(subscriptionId: Long): Transaction? = null

    override suspend fun deleteFutureBySubscriptionId(subscriptionId: Long, afterDate: Long) = Unit
}
