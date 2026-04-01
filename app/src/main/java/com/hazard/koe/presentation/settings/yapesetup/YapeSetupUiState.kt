package com.hazard.koe.presentation.settings.yapesetup

import com.hazard.koe.data.model.Account

enum class YapeSetupStep { INTRO, ACCOUNT, PERMISSION, DONE }

data class YapeSetupUiState(
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val currentStep: YapeSetupStep = YapeSetupStep.INTRO,
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastCapturedDescription: String = "",
    val lastCapturedAt: Long = 0L,
    val yapeEnabled: Boolean = false
)
