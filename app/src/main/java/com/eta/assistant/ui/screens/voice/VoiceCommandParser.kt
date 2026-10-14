package com.eta.assistant.ui.screens.voice

sealed class ParsedIntent {
    data class RecordSale(
        val productName: String,
        val quantity: Double,
        val amount: Double?,
        val customerName: String,
        val isCredit: Boolean = false
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
        "zero" to 0.0, "one" to 1.0, "two" to 2.0, "three" to 3.0, "four" to 4.0, "five" to 5.0,
        "six" to 6.0, "seven" to 7.0, "eight" to 8.0, "nine" to 9.0, "ten" to 10.0,
        "eleven" to 11.0, "twelve" to 12.0, "fifteen" to 15.0, "twenty" to 20.0,
        "fifty" to 50.0, "hundred" to 100.0, "half" to 0.5,
        // Hindi / Hinglish numbers
        "aadha" to 0.5, "adha" to 0.5, "आधा" to 0.5,
        "dedh" to 1.5, "डेढ़" to 1.5,
        "dhai" to 2.5, "ढाई" to 2.5,
        "ek" to 1.0, "do" to 2.0, "teen" to 3.0, "char" to 4.0, "paanch" to 5.0,
        "panch" to 5.0, "chhah" to 6.0, "saat" to 7.0, "aath" to 8.0, "nau" to 9.0, "das" to 10.0,
        "gyarah" to 11.0, "barah" to 12.0, "pandrah" to 15.0, "bees" to 20.0,
        "pachas" to 50.0, "sau" to 100.0, "dhasso" to 250.0, "paanso" to 500.0,
        "एक" to 1.0, "दो" to 2.0, "तीन" to 3.0, "चार" to 4.0, "पांच" to 5.0,
        "छह" to 6.0, "सात" to 7.0, "आठ" to 8.0, "नौ" to 9.0, "दस" to 10.0,
        "ग्यारह" to 11.0, "बारह" to 12.0, "पंद्रह" to 15.0, "बीस" to 20.0,
        "पचास" to 50.0, "सौ" to 100.0, "पांच सौ" to 500.0,
        // Tamil numbers
        "ஒன்று" to 1.0, "இரண்டு" to 2.0, "மூன்று" to 3.0, "நான்கு" to 4.0, "ஐந்து" to 5.0,
        "ஆறு" to 6.0, "ஏழு" to 7.0, "எட்டு" to 8.0, "ஒன்பது" to 9.0, "பத்து" to 10.0,
        "அரை" to 0.5, "நூறு" to 100.0, "ஐந்நூறு" to 500.0,
        // Bengali numbers
        "এক" to 1.0, "দুই" to 2.0, "তিন" to 3.0, "চার" to 4.0, "পাঁচ" to 5.0,
        "ছয়" to 6.0, "সাত" to 7.0, "আট" to 8.0, "নয়" to 9.0, "দশ" to 10.0,
        "আধ" to 0.5, "দেড়" to 1.5, "আড়াই" to 2.5, "একশো" to 100.0, "পাঁচশো" to 500.0
    )

    private val creditKeywords = listOf(
        "udhar", "udhaar", "udhaari", "khata", "khate", "credit", "due", "dues", "baki",
        "उधार", "उधारी", "खाता", "खाते", "बकाया",
        "கடன்", "பாக்கி",
        "বাকি", "ধার", "বকেয়া"
    )

    fun parse(text: String): ParsedIntent {
        val clean = text.trim()
        val lower = clean.lowercase()

        // 1. Show dues / debts query (English, Hindi, Tamil, Bengali)
        val hasDuesWord = lower.contains("due") || lower.contains("udhaari") || lower.contains("kiske kitne baki") ||
                lower.contains("बकाया") || lower.contains("उधारी") ||
                lower.contains("நிலுவை") || lower.contains("பாக்கி") ||
                lower.contains("বকেয়া") || lower.contains("বাকি")

        val isQueryWord = lower.contains("show") || lower.contains("what") || lower.contains("kitne") ||
                lower.contains("list") || lower.contains("dikhao") || lower.contains("dikhaye") ||
                lower.contains("காட்டு") || lower.contains("দেখাও") ||
                lower == "show dues" || lower == "show my dues" || lower == "बकाया दिखाओ" ||
                lower == "நிலுவை காட்டு" || lower == "বকেয়া দেখাও" || lower == "udhaari"

        if (hasDuesWord && (isQueryWord || !clean.any { it.isDigit() })) {
            return ParsedIntent.ShowDues
        }

        // 2. Top selling items (English, Hindi, Tamil, Bengali)
        if (lower.contains("top selling") || lower.contains("best selling") || lower.contains("sabse zyada") ||
            lower.contains("ज्यादा बिकने") || lower.contains("बिक्री रिपोर्ट") ||
            lower.contains("அதிகம் விற்பனை") || lower.contains("விற்பனை அறிக்கை") ||
            lower.contains("বেশি বিক্রি") || lower.contains("বিক্রয় রিপোর্ট")
        ) {
            return ParsedIntent.ShowTopSelling
        }

        // 3. Add to stock / inventory
        if (lower.contains("stock") || lower.contains("inventory") || lower.startsWith("add ") ||
            lower.contains("स्टॉक") || lower.contains("இருப்பு") || lower.contains("স্টক")
        ) {
            val addRegex = Regex("""(?:add|जोड़ें|சேர்|যোগ)\s+(\d+(?:\.\d+)?|[a-z\u0900-\u0D7F]+)\s*(?:kg|kilo|packet|packets|box|boxes|l|litres|bottle|bottles|किलो|கிலோ|কেজি)?\s*(?:of)?\s+([a-z0-9\s\u0900-\u0D7F]+?)\s+(?:to\s+(?:stock|inventory)|में|இல்|এ)?""", RegexOption.IGNORE_CASE)
            val match = addRegex.find(lower)
            if (match != null) {
                val qtyStr = match.groupValues[1]
                val qty = parseQuantity(qtyStr)
                val item = match.groupValues[2].trim().ifBlank { "Item" }
                return ParsedIntent.AddStock(productName = item, quantity = qty, unit = "units")
            }
        }

        val isCredit = creditKeywords.any { lower.contains(it) }

        // 4. Hindi/Hinglish SOV pattern: <amount> rupay ki <product> <customer> ke khate mein (likh do)
        // e.g. "500 rupay ki chini Ramesh ke khate mein likh do"
        val sovPattern1 = Regex(
            """(?:₹|rs\.?)?\s*(\d+(?:\.\d+)?)\s*(?:rupay|rupaye|rupees?|rs\.?|₹|रुपये|ரூபாய்|টাকা)?\s*(?:ki|ka|of)?\s+([a-zA-Z\u0900-\u0D7F]+)\s+([a-zA-Z\u0900-\u0D7F]+)\s+(?:ke\s+khate\s+(?:mein|me)|khate\s+(?:mein|me)|ko|ke\s+liye|ke|for)\b.*""",
            RegexOption.IGNORE_CASE
        )
        val sovMatch1 = sovPattern1.find(clean)
        if (sovMatch1 != null) {
            val amt = sovMatch1.groupValues[1].toDoubleOrNull()
            val prod = sovMatch1.groupValues[2].trim()
            val cust = sovMatch1.groupValues[3].trim().replaceFirstChar { it.uppercase() }
            return ParsedIntent.RecordSale(
                productName = prod,
                quantity = 1.0,
                amount = amt,
                customerName = cust,
                isCredit = isCredit
            )
        }

        // 5. Customer-first patterns:
        // Customer with quantity and product: e.g. "Ramesh 2 kg rice udhaar" or "Ramesh 2 kg chini 100 rupay"
        val custQtyProdPattern = Regex(
            """^([a-zA-Z\u0900-\u0D7F]+)\s+(\d+(?:\.\d+)?|[a-zA-Z\u0900-\u0D7F]+)\s*(?:kg|kilo|packet|packets|box|boxes|l|litres|bottle|bottles|किलो|கிலோ|কেজি)?\s+([a-zA-Z\u0900-\u0D7F]+)(?:\s+(?:₹|rs\.?)?\s*(\d+(?:\.\d+)?)\s*(?:rupay|rupaye|rupees?|rs\.?|₹|रुपये)?)?.*""",
            RegexOption.IGNORE_CASE
        )
        val custQtyMatch = custQtyProdPattern.find(clean)
        if (custQtyMatch != null) {
            val candidateCust = custQtyMatch.groupValues[1].trim()
            if (!listOf("record", "add", "sale", "show").contains(candidateCust.lowercase())) {
                val qty = parseQuantity(custQtyMatch.groupValues[2])
                val prod = custQtyMatch.groupValues[3].trim()
                val amt = custQtyMatch.groupValues[4].toDoubleOrNull()
                return ParsedIntent.RecordSale(
                    productName = prod,
                    quantity = qty,
                    amount = amt,
                    customerName = candidateCust.replaceFirstChar { it.uppercase() },
                    isCredit = isCredit
                )
            }
        }

        // Customer with amount only or amount + customer:
        // e.g. "Ramesh ke khate mein 500 rupay" or "Ramesh 500 rupay"
        val custAmtPattern = Regex(
            """^([a-zA-Z\u0900-\u0D7F]+)\s+(?:ke\s+khate\s+(?:mein|me)\s+|ko\s+)?(?:₹|rs\.?)?\s*(\d+(?:\.\d+)?)\s*(?:rupay|rupaye|rupees?|rs\.?|₹|रुपये)?.*""",
            RegexOption.IGNORE_CASE
        )
        val custAmtMatch = custAmtPattern.find(clean)
        if (custAmtMatch != null) {
            val candidateCust = custAmtMatch.groupValues[1].trim()
            if (!listOf("record", "add", "sale", "show").contains(candidateCust.lowercase())) {
                val amt = custAmtMatch.groupValues[2].toDoubleOrNull()
                if (amt != null && amt > 0) {
                    return ParsedIntent.RecordSale(
                        productName = "Store Item",
                        quantity = 1.0,
                        amount = amt,
                        customerName = candidateCust.replaceFirstChar { it.uppercase() },
                        isCredit = isCredit
                    )
                }
            }
        }

        // 6. English backward compatible patterns
        // "Record a ₹500 sale for Ravi"
        val saleForRegex = Regex("""(?:record\s+(?:a\s+)?)?(?:₹|rs\.?|inr|টাকা|ரூபாய்|रुपये)?\s*(\d+(?:\.\d+)?)\s*(?:sale|rupees?|रुपये|ரூபாய்|টাকা)?\s*(?:for|to|ke liye|के लिए|க்கு|র জন্য)\s+([a-z\s\u0900-\u0D7F]+)""", RegexOption.IGNORE_CASE)
        val saleForMatch = saleForRegex.find(lower)
        if (saleForMatch != null) {
            val amount = saleForMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val customer = saleForMatch.groupValues[2].trim().replaceFirstChar { it.uppercase() }
            return ParsedIntent.RecordSale(
                productName = "Store Item",
                quantity = 1.0,
                amount = amount,
                customerName = customer,
                isCredit = isCredit
            )
        }

        // "sale of 2 notebooks for Ravi"
        val saleItemForRegex = Regex("""(?:sale\s+of\s+)(\d+(?:\.\d+)?|[a-z\u0900-\u0D7F]+)\s+([a-z\s\u0900-\u0D7F]+?)\s+(?:for|to|के लिए|க்கு|র জন্য)\s+([a-z\s\u0900-\u0D7F]+)""", RegexOption.IGNORE_CASE)
        val matchItem = saleItemForRegex.find(lower)
        if (matchItem != null) {
            val qty = parseQuantity(matchItem.groupValues[1])
            val product = matchItem.groupValues[2].trim()
            val customer = matchItem.groupValues[3].trim().replaceFirstChar { it.uppercase() }
            return ParsedIntent.RecordSale(
                productName = product,
                quantity = qty,
                amount = null,
                customerName = customer,
                isCredit = isCredit
            )
        }

        // Simple amount and customer: "₹500 Ramesh" or "500 Ramesh"
        val simpleAmtCust = Regex("""^(?:₹|rs\.?)?\s*(\d+(?:\.\d+)?)\s+([a-zA-Z\u0900-\u0D7F]+)$""", RegexOption.IGNORE_CASE)
        val simpleMatch = simpleAmtCust.find(clean)
        if (simpleMatch != null) {
            val amt = simpleMatch.groupValues[1].toDoubleOrNull()
            val cust = simpleMatch.groupValues[2].trim().replaceFirstChar { it.uppercase() }
            if (amt != null) {
                return ParsedIntent.RecordSale(
                    productName = "Store Item",
                    quantity = 1.0,
                    amount = amt,
                    customerName = cust,
                    isCredit = isCredit
                )
            }
        }

        return ParsedIntent.Unknown(text)
    }

    private fun parseQuantity(str: String): Double {
        return str.toDoubleOrNull() ?: numberWords[str.lowercase()] ?: 1.0
    }
}
