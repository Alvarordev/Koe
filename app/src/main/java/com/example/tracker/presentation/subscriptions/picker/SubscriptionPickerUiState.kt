package com.example.tracker.presentation.subscriptions.picker

import com.example.tracker.presentation.subscriptions.KnownSubscription
import com.example.tracker.presentation.subscriptions.SubscriptionIconCatalog

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
