package com.example.tracker.data.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.example.tracker.domain.model.YapeOcrResult
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.TimeZone

class YapeImageOcrProcessor(private val context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build())

    private val amountRegex = Regex("""S/\s?([\d,]+\.?\d{0,2})""")
    private val dateRegex = Regex("""(\d{1,2})\s+(ene|feb|mar|abr|may|jun|jul|ago|sep|oct|nov|dic)\.?\s+(\d{4})""", RegexOption.IGNORE_CASE)
    private val timeRegex = Regex("""(\d{1,2}):(\d{2})\s*(a\.\s*m\.|p\.\s*m\.)""", RegexOption.IGNORE_CASE)
    private val operationRegex = Regex("""(?:operaci[oó]n|Nro\.?\s*de\s*operaci[oó]n)\s*(\d+)""", RegexOption.IGNORE_CASE)

    private val spanishMonths = mapOf(
        "ene" to 0, "feb" to 1, "mar" to 2, "abr" to 3,
        "may" to 4, "jun" to 5, "jul" to 6, "ago" to 7,
        "sep" to 8, "oct" to 9, "nov" to 10, "dic" to 11
    )

    private val knownLabels = setOf(
        "yapeaste", "te yapearon", "destino", "origen",
        "nro. de operación", "nro de operación", "operación",
        "fecha", "monto", "total", "comisión"
    )

    suspend fun extractFromUri(uri: Uri): YapeOcrResult? {
        val image = InputImage.fromFilePath(context, uri)
        val visionText = recognizer.process(image).await()
        return parseOcrText(visionText)
    }

    private fun parseOcrText(visionText: Text): YapeOcrResult? {
        val fullText = visionText.text

        if (!fullText.contains("Yapeaste", ignoreCase = true)) return null

        val amountMatch = amountRegex.find(fullText) ?: return null
        val amountCents = parseAmountToCents(amountMatch.groupValues[1])

        val recipientName = extractRecipientName(visionText)

        val dateMatch = dateRegex.find(fullText)
        val timeMatch = timeRegex.find(fullText)
        val dateMillis = if (dateMatch != null) parseDateTimeToMillis(dateMatch, timeMatch) else null

        val operationMatch = operationRegex.find(fullText)
        val operationNumber = operationMatch?.groupValues?.get(1)

        return YapeOcrResult(
            amountCents = amountCents,
            recipientName = recipientName,
            dateMillis = dateMillis,
            operationNumber = operationNumber
        )
    }

    private fun extractRecipientName(visionText: Text): String? {
        val lines = visionText.textBlocks.flatMap { it.lines }.map { it.text.trim() }

        var foundAmount = false
        for (line in lines) {
            if (amountRegex.containsMatchIn(line)) {
                foundAmount = true
                continue
            }
            if (!foundAmount) continue

            if (dateRegex.containsMatchIn(line)) break

            val normalized = line.lowercase()
            if (knownLabels.any { normalized.startsWith(it) }) continue
            if (operationRegex.containsMatchIn(line)) continue

            if (line.length in 2..50 && line.first().isUpperCase()) {
                return line
            }
        }
        return null
    }

    private fun parseDateTimeToMillis(dateMatch: MatchResult, timeMatch: MatchResult?): Long? {
        val day = dateMatch.groupValues[1].toIntOrNull() ?: return null
        val monthStr = dateMatch.groupValues[2].lowercase()
        val year = dateMatch.groupValues[3].toIntOrNull() ?: return null
        val month = spanishMonths[monthStr] ?: return null

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Lima"))
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (timeMatch != null) {
            var hour = timeMatch.groupValues[1].toIntOrNull() ?: 0
            val minute = timeMatch.groupValues[2].toIntOrNull() ?: 0
            val period = timeMatch.groupValues[3].replace(".", "").replace(" ", "").lowercase()

            if (period == "pm" && hour < 12) hour += 12
            if (period == "am" && hour == 12) hour = 0

            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
        }

        return calendar.timeInMillis
    }

    private fun parseAmountToCents(amountStr: String): Long {
        val cleaned = amountStr.replace(",", "")
        val value = cleaned.toDoubleOrNull() ?: 0.0
        return (value * 100).toLong()
    }
}
