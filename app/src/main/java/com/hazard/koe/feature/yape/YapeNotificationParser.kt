package com.hazard.koe.feature.yape

data class ParsedYapeTransaction(
    val amountCents: Long,
    val counterpart: String,
    val description: String
)

class YapeNotificationParser {
    private val amountRegex = Regex("""S/\s?([\d,]+\.?\d{0,2})""")

    fun parse(title: String, text: String): ParsedYapeTransaction? {
        val titleLower = title.lowercase()
        val textLower = text.lowercase()

        val isIncome = "recibiste" in titleLower || "recibido" in titleLower || "recibiste" in textLower
        if (!isIncome) return null

        val amountMatch = amountRegex.find(text) ?: return null
        val amountStr = amountMatch.groupValues[1].replace(",", "")
        val amountCents = (amountStr.toDoubleOrNull() ?: return null).let { (it * 100).toLong() }

        val counterpart = Regex("""de (.+?)(?:\s*${'$'}|\.|\s{2,})""").find(text)?.groupValues?.get(1)?.trim()
            ?: Regex("""^(.+?)\s+te envió""").find(text)?.groupValues?.get(1)?.trim()
            ?: "Yape"

        return ParsedYapeTransaction(amountCents, counterpart, "Yape recibido: $counterpart")
    }
}
