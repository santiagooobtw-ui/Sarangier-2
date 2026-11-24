package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object Formatters {

    private val esCoLocale = Locale("es", "CO")
    private val copNumberFormat: NumberFormat = NumberFormat.getCurrencyInstance(esCoLocale).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    private val thousandsFormat: DecimalFormat = DecimalFormat(
        "#,##0",
        DecimalFormatSymbols(esCoLocale).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
    )

    fun formatCOP(value: Long): String {
        val sign = if (value < 0) "-" else ""
        // Use NumberFormat('es-CO', currency COP) semantics with clean '$12.500' presentation
        val formattedDigits = thousandsFormat.format(abs(value))
        val currencyCheck = copNumberFormat.format(0)
        val symbol = if (currencyCheck.contains("$")) "$" else "$"
        return "$sign$symbol$formattedDigits"
    }

    fun formatSignedCOP(value: Long): String {
        return when {
            value > 0 -> "+${formatCOP(value)}"
            value < 0 -> formatCOP(value)
            else -> formatCOP(0)
        }
    }

    fun parseCOPInput(raw: String): Long? {
        val digitsOnly = raw.filter { it.isDigit() }
        return if (digitsOnly.isEmpty()) null else digitsOnly.toLongOrNull()
    }

    fun todayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun yesterdayDate(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    fun currentTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        return sdf.format(Date())
    }

    fun formatDateLabel(dateStr: String): String {
        val today = todayDate()
        val yesterday = yesterdayDate()
        return when (dateStr) {
            today -> "Hoy ($dateStr)"
            yesterday -> "Ayer ($dateStr)"
            else -> dateStr
        }
    }
}
