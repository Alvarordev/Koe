package com.hazard.koe.data.repository.impl

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.hazard.koe.data.enums.TransactionType
import com.hazard.koe.domain.model.VoiceTransactionInferenceRequest
import com.hazard.koe.domain.model.VoiceTransactionInferenceResult
import com.hazard.koe.domain.repository.VoiceTransactionInferenceRepository
import org.json.JSONObject

class FirebaseAiVoiceTransactionInferenceRepositoryImpl(
) : VoiceTransactionInferenceRepository {
    override suspend fun infer(request: VoiceTransactionInferenceRequest): Result<VoiceTransactionInferenceResult> {
        return runCatching {
            val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                modelName = MODEL_NAME,
                generationConfig = generationConfig {
                    temperature = 0f
                    maxOutputTokens = MAX_OUTPUT_TOKENS
                    responseMimeType = "application/json"
                }
            )

            val response = model.generateContent(
                content {
                    if (request.audioBytes.isNotEmpty()) {
                        inlineData(request.audioBytes, request.audioMimeType)
                    }
                    text(buildPrompt(request))
                }
            )
            val rawText = response.text?.trim().orEmpty()
            if (rawText.isBlank()) {
                throw IllegalStateException("Firebase AI returned an empty inference response")
            }
            val jsonText = sanitizeJsonResponse(rawText)
            parseResult(jsonText)
        }
    }

    private fun buildPrompt(request: VoiceTransactionInferenceRequest): String {
        val accountsJson = request.accounts.joinToString(",", prefix = "[", postfix = "]") {
            "{\"id\":${it.id},\"name\":\"${escapeJson(it.name)}\",\"currencyCode\":\"${escapeJson(it.currencyCode)}\"}"
        }
        val categoriesJson = request.categories.joinToString(",", prefix = "[", postfix = "]") {
            "{\"id\":${it.id},\"name\":\"${escapeJson(it.name)}\",\"type\":\"${it.type.name}\"}"
        }

        return """
You are a strict transaction parser for a finance app.
Infer one transaction from the user voice audio and return ONLY valid JSON.

Rules:
- amountMinor must be integer minor units (for example, 12.34 => 1234).
- transactionType must be EXPENSE or INCOME.
- categoryId and accountId must be chosen from provided lists, or null when unknown.
- confidence must be a decimal from 0.0 to 1.0.
- description should be concise and useful.
- reasoning can be short.
- Do not include markdown or extra keys.

Input:
{
  "localeTag": "${escapeJson(request.localeTag)}",
  "audioProvided": true,
  "audioMimeType": "${escapeJson(request.audioMimeType)}",
  "accounts": $accountsJson,
  "categories": $categoriesJson
}

Return JSON with exactly these keys:
{
  "amountMinor": 0,
  "transactionType": "EXPENSE",
  "categoryId": null,
  "accountId": null,
  "description": null,
  "confidence": 0.0,
  "reasoning": null
}
""".trimIndent()
    }

    private fun parseResult(jsonText: String): VoiceTransactionInferenceResult {
        val json = JSONObject(jsonText)
        val transactionType = parseTransactionType(json.optString("transactionType", "EXPENSE"))
        val confidence = json.optDouble("confidence", 0.0).toFloat().coerceIn(0f, 1f)

        return VoiceTransactionInferenceResult(
            amountMinor = parseAmountMinor(json),
            transactionType = transactionType,
            categoryId = json.optNullableLong("categoryId"),
            accountId = json.optNullableLong("accountId"),
            description = json.optNullableString("description"),
            confidence = confidence,
            reasoning = json.optNullableString("reasoning")
        )
    }

    private fun parseAmountMinor(json: JSONObject): Long {
        val raw = json.opt("amountMinor") ?: 0
        return when (raw) {
            is Number -> raw.toLong()
            is String -> raw.trim().toLongOrNull() ?: 0L
            else -> 0L
        }.coerceAtLeast(0L)
    }

    private fun parseTransactionType(raw: String): TransactionType {
        return runCatching { TransactionType.valueOf(raw.trim().uppercase()) }
            .getOrDefault(TransactionType.EXPENSE)
    }

    private fun sanitizeJsonResponse(raw: String): String {
        val trimmed = raw.trim()
        if (!trimmed.startsWith("```")) return trimmed

        val withoutStart = trimmed.removePrefix("```json").removePrefix("```").trim()
        return withoutStart.removeSuffix("```").trim()
    }

    private fun escapeJson(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun JSONObject.optNullableLong(key: String): Long? {
        val value = opt(key)
        return when {
            value == null || value == JSONObject.NULL -> null
            value is Number -> value.toLong()
            value is String -> value.trim().toLongOrNull()
            else -> null
        }
    }

    private fun JSONObject.optNullableString(key: String): String? {
        val value = opt(key)
        return when {
            value == null || value == JSONObject.NULL -> null
            else -> value.toString().trim().ifBlank { null }
        }
    }

    private companion object {
        private const val MODEL_NAME = "gemini-2.5-flash-lite"
        private const val MAX_OUTPUT_TOKENS = 256
    }
}
