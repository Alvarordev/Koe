package com.hazard.koe.data.voice

import com.hazard.koe.data.enums.CategoryType
import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.domain.model.VoiceAccountContext
import com.hazard.koe.domain.model.VoiceCategoryContext
import com.hazard.koe.domain.model.VoiceTransactionInferenceRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceTransactionParserTest {

    private val parser = VoiceTransactionParser()

    @Test
    fun parse_expenseText_extractsAmountAndMatchesEntities() {
        val result = parser.parse(
            VoiceTransactionInferenceRequest(
                transcript = "Gaste 25.50 en comida con BCP",
                localeTag = "es-PE",
                accounts = listOf(VoiceAccountContext(10L, "BCP", "PEN")),
                categories = listOf(
                    VoiceCategoryContext(1L, "Comida", CategoryType.EXPENSE),
                    VoiceCategoryContext(2L, "Sueldo", CategoryType.INCOME)
                )
            )
        )

        assertEquals(2550L, result.amountMinor)
        assertEquals(TransactionType.EXPENSE, result.transactionType)
        assertEquals(10L, result.accountId)
        assertEquals(1L, result.categoryId)
        assertNotNull(result.description)
        assertTrue(result.confidence > 0.7f)
    }

    @Test
    fun parse_incomeText_infersIncomeType() {
        val result = parser.parse(
            VoiceTransactionInferenceRequest(
                transcript = "Recibí 1200 de sueldo",
                localeTag = "es-PE",
                accounts = emptyList(),
                categories = listOf(
                    VoiceCategoryContext(8L, "Sueldo", CategoryType.INCOME)
                )
            )
        )

        assertEquals(120000L, result.amountMinor)
        assertEquals(TransactionType.INCOME, result.transactionType)
        assertEquals(8L, result.categoryId)
    }
}
