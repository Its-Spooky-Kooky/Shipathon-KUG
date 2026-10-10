package com.eta.assistant.ui.screens.voice

sealed class ParsedIntent {
    data class RecordSale(
        val productName: String,
        val quantity: Double,
        val amount: Double?,
        val customerName: String
    ) : ParsedIntent()

    data class AddStock(
        val productName: String,
        val quantity: Double,
        val unit: String
    ) : ParsedIntent()

    object ShowDues : ParsedIntent()
    object ShowTopSelling : ParsedIntent()
    data class Unknown(val rawText: String) : ParsedIntent()
}

object VoiceCommandParser {

    private val numberWords = mapOf(
        "one" to 1.0, "two" to 2.0, "three" to 3.0, "four" to 4.0, "five" to 5.0,
        "six" to 6.0, "seven" to 7.0, "eight" to 8.0, "nine" to 9.0, "ten" to 10.0,
        "ek" to 1.0, "do" to 2.0, "teen" to 3.0, "char" to 4.0, "paanch" to 5.0,
        "एक" to 1.0, "दो" to 2.0, "तीन" to 3.0, "चार" to 4.0, "पांच" to 5.0,
        "ஒன்று" to 1.0, "இரண்டு" to 2.0, "மூன்று" to 3.0, "நான்கு" to 4.0, "ஐந்து" to 5.0,
        "এক" to 1.0, "দুই" to 2.0, "তিন" to 3.0, "চার" to 4.0, "পাঁচ" to 5.0
    )

    fun parse(text: String): ParsedIntent {
        val clean = text.trim().lowercase()

        // 1. Show dues / debts (English, Hindi, Tamil, Bengali)
        if (clean.contains("due") || clean.contains("udhaari") || clean.contains("kiske kitne baki") ||
            clean.contains("बकाया") || clean.contains("उधारी") ||
            clean.contains("நிலுவை") || clean.contains("பாக்கி") ||
            clean.contains("বকেয়া") || clean.contains("বাকি")
        ) {
            return ParsedIntent.ShowDues
        }

        // 2. Top selling items (English, Hindi, Tamil, Bengali)
        if (clean.contains("top selling") || clean.contains("best selling") || clean.contains("sabse zyada") ||
            clean.contains("ज्यादा बिकने") || clean.contains("बिक्री रिपोर्ट") ||
            clean.contains("அதிகம் விற்பனை") || clean.contains("விற்பனை அறிக்கை") ||
            clean.contains("বেশি বিক্রি") || clean.contains("বিক্রয় রিপোর্ট")
        ) {
            return ParsedIntent.ShowTopSelling
        }

        // 3. Add to stock / inventory
        if (clean.contains("stock") || clean.contains("inventory") || clean.startsWith("add ") ||
            clean.contains("स्टॉक") || clean.contains("இருப்பு") || clean.contains("স্টক")
        ) {
            val addRegex = Regex("""(?:add|जोड़ें|சேர்|যোগ)\s+(\d+(?:\.\d+)?|[a-z\u0900-\u0D7F]+)\s*(?:kg|packet|packets|box|boxes|l|litres|bottle|bottles|किलो|கிலோ|কেজি)?\s*(?:of)?\s+([a-z0-9\s\u0900-\u0D7F]+?)\s+(?:to\s+(?:stock|inventory)|में|இல்|এ)?""", RegexOption.IGNORE_CASE)
            val match = addRegex.find(clean)
            if (match != null) {
                val qtyStr = match.groupValues[1]
                val qty = parseQuantity(qtyStr)
                val item = match.groupValues[2].trim().ifBlank { "Item" }
                return ParsedIntent.AddStock(productName = item, quantity = qty, unit = "units")
            }
        }

        // 4. Record sale
        val saleForRegex = Regex("""(?:record\s+(?:a\s+)?)?(?:₹|rs\.?|inr|টাকা|ரூபாய்|रुपये)?\s*(\d+(?:\.\d+)?)\s*(?:sale|rupees?|रुपये|ரூபாய்|টাকা)?\s*(?:for|to|ke liye|के लिए|க்கு|র জন্য)\s+([a-z\s\u0900-\u0D7F]+)""", RegexOption.IGNORE_CASE)
        val saleForMatch = saleForRegex.find(clean)
        if (saleForMatch != null) {
            val amount = saleForMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val customer = saleForMatch.groupValues[2].trim().replaceFirstChar { it.uppercase() }
            return ParsedIntent.RecordSale(
                productName = "Store Item",
                quantity = 1.0,
                amount = amount,
                customerName = customer
            )
        }

        val saleItemForRegex = Regex("""(?:sale\s+of\s+)(\d+(?:\.\d+)?|[a-z\u0900-\u0D7F]+)\s+([a-z\s\u0900-\u0D7F]+?)\s+(?:for|to|के लिए|க்கு|র জন্য)\s+([a-z\s\u0900-\u0D7F]+)""", RegexOption.IGNORE_CASE)
        val matchItem = saleItemForRegex.find(clean)
        if (matchItem != null) {
            val qty = parseQuantity(matchItem.groupValues[1])
            val product = matchItem.groupValues[2].trim()
            val customer = matchItem.groupValues[3].trim().replaceFirstChar { it.uppercase() }
            return ParsedIntent.RecordSale(
                productName = product,
                quantity = qty,
                amount = null,
                customerName = customer
            )
        }

        return ParsedIntent.Unknown(text)
    }

    private fun parseQuantity(str: String): Double {
        return str.toDoubleOrNull() ?: numberWords[str.lowercase()] ?: 1.0
    }
}
