package com.example.tracker.data.repository

import com.example.tracker.data.enums.RecurringType
import com.example.tracker.data.model.RecurringRule
import com.example.tracker.data.model.relations.RecurringRuleWithDetails
import kotlinx.coroutines.flow.Flow

interface RecurringRuleRepository {
    fun getAll(): Flow<List<RecurringRuleWithDetails>>
    fun getByType(type: RecurringType): Flow<List<RecurringRuleWithDetails>>
    suspend fun create(rule: RecurringRule): Long
    suspend fun update(rule: RecurringRule)
    suspend fun deactivate(id: Long)
    suspend fun processDueRules()
}
