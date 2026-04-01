package com.hazard.koe.domain.repository

import com.hazard.koe.data.enums.RecurringType
import com.hazard.koe.data.model.RecurringRule
import com.hazard.koe.data.model.relations.RecurringRuleWithDetails
import kotlinx.coroutines.flow.Flow

interface RecurringRuleRepository {
    fun getAll(): Flow<List<RecurringRuleWithDetails>>
    fun getByType(type: RecurringType): Flow<List<RecurringRuleWithDetails>>
    suspend fun create(rule: RecurringRule): Long
    suspend fun update(rule: RecurringRule)
    suspend fun deactivate(id: Long)
    suspend fun processDueRules()
}
