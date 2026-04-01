package com.hazard.koe.data.voice

import com.hazard.koe.data.enums.CategoryType
import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.domain.model.VoiceTransactionInferenceRequest
import com.hazard.koe.domain.model.VoiceTransactionInferenceResult
import java.util.Locale

class VoiceTransactionParser {

    fun parse(request: VoiceTransactionInferenceRequest): VoiceTransactionInferenceResult {
        val transcriptText = request.transcript.orEmpty()
        val normalized = normalize(transcriptText)
        val amountMinor = extractAmountMinor(normalized) ?: 0L
        val transactionType = inferTransactionType(normalized)
        val matchedAccount = request.accounts.firstOrNull {
            normalized.contains(normalize(it.name))
        }
        val matchedCategory = request.categories.firstOrNull {
            it.type == transactionType.toCategoryType() && normalized.contains(normalize(it.name))
        }

        val confidence = buildConfidence(
            amountMinor = amountMinor,
            hasAccount = matchedAccount != null,
            hasCategory = matchedCategory != null,
            transcript = normalized,
            transactionType = transactionType
        )

        return VoiceTransactionInferenceResult(
            amountMinor = amountMinor,
            transactionType = transactionType,
            categoryId = matchedCategory?.id,
            accountId = matchedAccount?.id,
            description = transcriptText.trim().ifBlank { null },
            confidence = confidence,
            reasoning = "heuristic"
        )
    }

    private fun inferTransactionType(text: String): TransactionType {
        val incomeHints = listOf("ingreso", "me pagaron", "recibi", "recibí", "cobre", "cobré")
        val isIncome = incomeHints.any { text.contains(it) }
        return if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
    }

    private fun buildConfidence(
        amountMinor: Long,
        hasAccount: Boolean,
        hasCategory: Boolean,
        transcript: String,
        transactionType: TransactionType
    ): Float {
        var score = 0.35f
        if (amountMinor > 0) score += 0.3f
        if (hasAccount) score += 0.15f
        if (hasCategory) score += 0.1f
        if (transactionType == TransactionType.INCOME && transcript.contains("ingreso")) score += 0.05f
        if (transactionType == TransactionType.EXPENSE && transcript.contains("gaste")) score += 0.05f
        return score.coerceIn(0f, 1f)
    }

    private fun normalize(value: String): String {
        return value
            .lowercase(Locale.getDefault())
            .replace("á", "a")
            .replace("é", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ú", "u")
            .trim()
    }

    private fun extractAmountMinor(text: String): Long? {
        val regex = Regex("(\\d+[\\.,]?\\d{0,2})")
        val raw = regex.find(text)?.value ?: return null
        return decimalStringToMinorUnits(raw)
    }

    private fun decimalStringToMinorUnits(raw: String): Long? {
        val cleaned = raw.replace(" ", "")
        val hasDot = cleaned.contains('.')
        val hasComma = cleaned.contains(',')
        val decimalSeparator = when {
            hasDot && hasComma -> {
                if (cleaned.lastIndexOf('.') > cleaned.lastIndexOf(',')) '.' else ','
            }
            hasDot -> if (cleaned.substringAfterLast('.').length <= 2) '.' else null
            hasComma -> if (cleaned.substringAfterLast(',').length <= 2) ',' else null
            else -> null
        }

        val integerPart = StringBuilder()
        val decimalPart = StringBuilder()

        var inDecimal = false
        cleaned.forEach { char ->
            when {
                char.isDigit() && !inDecimal -> integerPart.append(char)
                char.isDigit() && inDecimal && decimalPart.length < 2 -> decimalPart.append(char)
                decimalSeparator != null && char == decimalSeparator -> inDecimal = true
            }
        }

        val intValue = integerPart.toString().ifBlank { "0" }.toLongOrNull() ?: return null
        val decimalValue = decimalPart.toString().padEnd(2, '0').ifBlank { "00" }.toLongOrNull() ?: return null
        return intValue * 100L + decimalValue
    }

    private fun TransactionType.toCategoryType(): CategoryType {
        return when (this) {
            TransactionType.EXPENSE -> CategoryType.EXPENSE
            TransactionType.INCOME -> CategoryType.INCOME
            else -> CategoryType.EXPENSE
        }
    }
}
