package com.hazard.koe.presentation.subscriptions.picker

import com.hazard.koe.presentation.subscriptions.KnownSubscription
import com.hazard.koe.presentation.subscriptions.SubscriptionIconCatalog

data class SubscriptionPickerUiState(
    val query: String = "",
) {
    val filtered: List<KnownSubscription>
        get() = if (query.isBlank()) SubscriptionIconCatalog.entries
                else SubscriptionIconCatalog.entries.filter {
                    it.name.contains(query, ignoreCase = true)
                }

    val showCreateNew: Boolean
        get() = query.isNotBlank() && filtered.isEmpty()
}
