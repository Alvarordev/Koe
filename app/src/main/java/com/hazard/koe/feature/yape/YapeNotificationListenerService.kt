package com.hazard.koe.feature.yape

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.hazard.koe.data.db.dao.ProcessedNotificationDao
import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.data.model.ProcessedNotification
import com.hazard.koe.data.model.Transaction
import com.hazard.koe.data.preferences.YapePreferences
import com.hazard.koe.domain.usecase.transaction.CreateTransactionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class YapeNotificationListenerService : NotificationListenerService(), KoinComponent {

    private val createTransaction: CreateTransactionUseCase by inject()
    private val yapePreferences: YapePreferences by inject()
    private val parser: YapeNotificationParser by inject()
    private val processedNotificationDao: ProcessedNotificationDao by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val YAPE_PACKAGE = "com.bcp.innovacxion.yapeapp"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != YAPE_PACKAGE) return
        val key = sbn.key

        scope.launch {
            if (processedNotificationDao.existsByDedupKey(key)) return@launch

            val enabled = yapePreferences.yapeEnabled.first()
            if (!enabled) return@launch

            val extras = sbn.notification.extras
            val title = extras.getString("android.title") ?: return@launch
            val text = extras.getCharSequence("android.text")?.toString() ?: return@launch

            val parsed = parser.parse(title, text) ?: return@launch

            val accountId = yapePreferences.defaultAccountId.first()
            val categoryId = yapePreferences.categoryIncomeId.first()

            if (accountId == 0L) return@launch
            if (categoryId == 0L) return@launch

            val transaction = Transaction(
                type = TransactionType.INCOME,
                amount = parsed.amountCents,
                description = parsed.description,
                accountId = accountId,
                categoryId = categoryId,
                date = sbn.postTime
            )
            createTransaction(transaction)

            processedNotificationDao.insert(
                ProcessedNotification(
                    dedupKey = key,
                    amount = parsed.amountCents,
                    type = "INCOME"
                )
            )

            yapePreferences.setLastCaptured(parsed.description, sbn.postTime)
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        scope.launch { yapePreferences.setYapeEnabled(false) }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
