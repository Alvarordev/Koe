package com.hazard.koe.presentation.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hazard.koe.data.model.Account
import com.hazard.koe.data.model.UserSubscription
import com.hazard.koe.domain.usecase.account.GetAccountsUseCase
import com.hazard.koe.domain.usecase.subscription.DeleteSubscriptionUseCase
import com.hazard.koe.domain.usecase.subscription.GetAllSubscriptionsUseCase
import com.hazard.koe.domain.usecase.subscription.GetSubscriptionServicesUseCase
import com.hazard.koe.domain.usecase.subscription.ProcessSubscriptionBillingUseCase
import com.hazard.koe.domain.usecase.subscription.SaveSubscriptionUseCase
import java.util.Locale
import com.hazard.koe.presentation.subscriptions.detail.SubscriptionDetailUiState
import com.hazard.koe.presentation.subscriptions.picker.SubscriptionPickerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val getAccounts: GetAccountsUseCase,
    private val getSubscriptionServices: GetSubscriptionServicesUseCase,
    private val saveSubscription: SaveSubscriptionUseCase,
    private val getAllSubscriptions: GetAllSubscriptionsUseCase,
    private val deleteSubscription: DeleteSubscriptionUseCase,
    private val processBilling: ProcessSubscriptionBillingUseCase
) : ViewModel() {

    private val _pickerState = MutableStateFlow(SubscriptionPickerUiState())
    val pickerState: StateFlow<SubscriptionPickerUiState> = _pickerState.asStateFlow()

    private val _detailState = MutableStateFlow(SubscriptionDetailUiState())
    val detailState: StateFlow<SubscriptionDetailUiState> = _detailState.asStateFlow()

    init {
        viewModelScope.launch {
            getAccounts().collect { accounts ->
                val active = accounts.filter { !it.isArchived }
                _detailState.update { state ->
                    state.copy(
                        accounts = accounts,
                        selectedAccount = state.selectedAccount ?: active.minByOrNull { it.sortOrder }
                    )
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        _pickerState.update { it.copy(query = query) }
    }

    fun initDetail(iconResName: String?) {
        val isCustom = iconResName == null
        val knownName = if (!isCustom) SubscriptionIconCatalog.entries.find { it.iconResName == iconResName }?.name ?: "" else ""
        _detailState.update { current ->
            SubscriptionDetailUiState(
                isCustom = isCustom,
                iconResName = iconResName,
                customName = knownName,
                accounts = current.accounts,
                selectedAccount = current.selectedAccount
            )
        }
        if (!isCustom) {
            viewModelScope.launch {
                val service = getSubscriptionServices().first().find { it.iconResName == iconResName }
                _detailState.update { it.copy(service = service) }
            }
        }
    }

    fun initDetailForEdit(subscriptionId: Long) {
        viewModelScope.launch {
            val item = getAllSubscriptions().first().find { it.subscription.id == subscriptionId } ?: return@launch
            val sub = item.subscription
            val amountDecimal = if (sub.amount % 100 == 0L) {
                (sub.amount / 100).toString()
            } else {
                String.format(Locale.US, "%.2f", sub.amount / 100.0)
            }
            _detailState.update { current ->
                SubscriptionDetailUiState(
                    editingId = sub.id,
                    editingCreatedAt = sub.createdAt,
                    isCustom = sub.iconResName == null,
                    iconResName = sub.iconResName,
                    customName = sub.customName ?: item.service?.name ?: "",
                    customEmoji = sub.customEmoji ?: "⭐",
                    amountString = amountDecimal,
                    billingDay = sub.billingDay,
                    accounts = current.accounts,
                    selectedAccount = current.accounts.find { it.id == sub.accountId } ?: current.selectedAccount,
                    service = item.service
                )
            }
        }
    }

    fun onAmountChange(s: String) {
        val filtered = s.filter { it.isDigit() || it == '.' }
        val dotCount = filtered.count { it == '.' }
        val valid = if (dotCount > 1) {
            val firstDot = filtered.indexOf('.')
            filtered.substring(0, firstDot + 1) + filtered.substring(firstDot + 1).replace(".", "")
        } else {
            filtered
        }
        _detailState.update { it.copy(amountString = valid, submitError = null) }
    }

    fun onBillingDayChange(day: Int) {
        _detailState.update { it.copy(billingDay = day.coerceIn(0, 31)) }
    }

    fun onSelectAccount(account: Account) {
        _detailState.update { it.copy(selectedAccount = account) }
    }

    fun onCustomNameChange(name: String) {
        _detailState.update { it.copy(customName = name) }
    }

    fun onEmojiChange(emoji: String) {
        _detailState.update { it.copy(customEmoji = emoji) }
    }

    fun onBillCurrentMonthChange(value: Boolean) {
        _detailState.update { it.copy(billCurrentMonth = value) }
    }

    fun submit() {
        val state = _detailState.value
        if (state.isSubmitting) return

        val account = state.selectedAccount
        if (account == null) {
            _detailState.update { it.copy(submitError = "Selecciona una cuenta") }
            return
        }

        val amountCents = amountStringToCents(state.amountString)
        if (amountCents == 0L) {
            _detailState.update { it.copy(submitError = "Ingresa un monto") }
            return
        }

        if (state.isCustom && state.customName.isBlank()) {
            _detailState.update { it.copy(submitError = "Ingresa el nombre de la suscripción") }
            return
        }

        val subscription = UserSubscription(
            id = state.editingId ?: 0L,
            serviceId = if (state.isCustom) null else state.service?.id,
            accountId = account.id,
            amount = amountCents,
            billingDay = state.billingDay.coerceAtLeast(1),
            currencyCode = account.currencyCode,
            customName = state.customName.ifBlank { null },
            customEmoji = if (state.isCustom) state.customEmoji else null,
            iconResName = if (state.isCustom) null else state.iconResName,
            createdAt = state.editingCreatedAt ?: System.currentTimeMillis()
        )

        val billCurrentMonth = state.billCurrentMonth
        _detailState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            try {
                val savedId = saveSubscription(subscription)
                processBilling(savedId, billCurrentMonth)
                _detailState.update { it.copy(isSubmitting = false, submitSuccess = true) }
            } catch (e: Exception) {
                _detailState.update { it.copy(isSubmitting = false, submitError = "Error al guardar") }
            }
        }
    }

    fun deleteCurrentSubscription() {
        val id = _detailState.value.editingId ?: return
        viewModelScope.launch {
            try {
                deleteSubscription(id)
                _detailState.update { it.copy(deleteSuccess = true) }
            } catch (e: Exception) {
                _detailState.update { it.copy(submitError = "Error al eliminar") }
            }
        }
    }

    fun resetDetail() {
        _detailState.update { current ->
            SubscriptionDetailUiState(
                accounts = current.accounts,
                selectedAccount = current.accounts.filter { !it.isArchived }.minByOrNull { it.sortOrder }
            )
        }
    }

    private fun amountStringToCents(s: String): Long {
        if (s.isBlank() || s == "." || s == "0") return 0L
        return ((s.toDoubleOrNull() ?: 0.0) * 100).toLong()
    }
}
