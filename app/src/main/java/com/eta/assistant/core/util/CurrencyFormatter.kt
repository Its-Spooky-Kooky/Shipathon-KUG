package com.eta.assistant.core.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun format(amount: Double, currencySymbol: String = "₹"): String {
        val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        return "$currencySymbol ${formatter.format(amount)}"
    }

    fun format(amountPaise: Long, currencySymbol: String = "₹"): String {
        return format(amountPaise / 100.0, currencySymbol)
    }
}

