package com.example.tracker.data.repository.impl

import com.example.tracker.data.db.dao.AccountDao
import com.example.tracker.data.db.dao.RecurringRuleDao
import com.example.tracker.data.db.dao.TransactionDao
import com.example.tracker.data.enums.FrequencyType
import com.example.tracker.data.enums.RecurringType
import com.example.tracker.data.enums.TransactionType
import com.example.tracker.data.model.RecurringRule
import com.example.tracker.data.model.Transaction
import com.example.tracker.data.model.relations.RecurringRuleWithDetails
import com.example.tracker.data.repository.RecurringRuleRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class RecurringRuleRepositoryImpl(
    private val recurringRuleDao: RecurringRuleDao,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao
) : RecurringRuleRepository {

    override fun getAll(): Flow<List<RecurringRuleWithDetails>> = recurringRuleDao.getAll()

    override fun getByType(type: RecurringType): Flow<List<RecurringRuleWithDetails>> =
        recurringRuleDao.getByType(type)

    override suspend fun create(rule: RecurringRule): Long = recurringRuleDao.insert(rule)

    override suspend fun update(rule: RecurringRule) = recurringRuleDao.update(rule)

    override suspend fun deactivate(id: Long) = recurringRuleDao.deactivate(id)

    override suspend fun processDueRules() {
        val now = System.currentTimeMillis()
        val dueRules = recurringRuleDao.getDueRules(now)
        for (rule in dueRules) {
            var next = rule.nextOccurrence
            while (next <= now) {
                val txnType = when (rule.type) {
                    RecurringType.RECURRING_INCOME -> TransactionType.INCOME
                    else -> TransactionType.EXPENSE
                }
                transactionDao.insert(
                    Transaction(
                        type = txnType,
                        amount = rule.amount,
                        accountId = rule.accountId,
                        categoryId = rule.categoryId,
                        subscriptionId = if (rule.type == RecurringType.SUBSCRIPTION) rule.id else null,
                        date = next,
                        isRecurring = true,
                        recurringRuleId = rule.id
                    )
                )
                next = computeNextOccurrence(next, rule)
            }
            recurringRuleDao.updateNextOccurrence(rule.id, next)
            if (rule.endDate != null && next > rule.endDate) {
                recurringRuleDao.deactivate(rule.id)
            }
        }
    }

    private fun computeNextOccurrence(from: Long, rule: RecurringRule): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = from }
        when (rule.frequencyType) {
            FrequencyType.DAILY -> cal.add(Calendar.DAY_OF_YEAR, rule.frequencyInterval)
            FrequencyType.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, rule.frequencyInterval)
            FrequencyType.MONTHLY -> cal.add(Calendar.MONTH, rule.frequencyInterval)
            FrequencyType.YEARLY -> cal.add(Calendar.YEAR, rule.frequencyInterval)
        }
        return cal.timeInMillis
    }
}
