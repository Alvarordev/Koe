package com.example.tracker.presentation.subscriptions.detail

import com.example.tracker.data.model.Account
import com.example.tracker.data.model.SubscriptionService

data class SubscriptionDetailUiState(
    val service: SubscriptionService? = null,
    val iconResName: String? = null,
    val customName: String = "",
    val customEmoji: String = "⭐",
    val amountString: String = "",
    val billingDay: Int = 1,
    val selectedAccount: Account? = null,
    val accounts: List<Account> = emptyList(),
    val isCustom: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val submitError: String? = null,
    val editingId: Long? = null,
    val editingCreatedAt: Long? = null,
    val deleteSuccess: Boolean = false
)
