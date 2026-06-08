package dev.ceccon.pieno.core

import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private val IT = Locale("it", "IT")

object Format {

    fun pricePerLiter(value: Double): String = String.format(IT, "%.3f", value)

    fun euro(value: Double): String = String.format(IT, "%.2f €", value)

    fun euroSigned(value: Double): String =
        (if (value >= 0) "+" else "-") + String.format(IT, "%.2f €", kotlin.math.abs(value))

    fun liters(value: Double): String = String.format(IT, "%.2f L", value)

    fun dateLong(epochDay: Long): String {
        val d = LocalDate.ofEpochDay(epochDay)
        val month = d.month.getDisplayName(TextStyle.FULL, IT)
        return "${d.dayOfMonth} $month ${d.year}"
    }

    fun dateMedium(epochDay: Long): String {
        val d = LocalDate.ofEpochDay(epochDay)
        val month = d.month.getDisplayName(TextStyle.SHORT, IT).trimEnd('.')
        return "${d.dayOfMonth} $month"
    }

    fun dateNumeric(epochDay: Long): String {
        val d = LocalDate.ofEpochDay(epochDay)
        return String.format(IT, "%02d/%02d/%04d", d.dayOfMonth, d.monthValue, d.year)
    }

    fun monthYear(epochDay: Long): String {
        val d = LocalDate.ofEpochDay(epochDay)
        val month = d.month.getDisplayName(TextStyle.FULL, IT)
        return month.replaceFirstChar { it.uppercase(IT) } + " ${d.year}"
    }

    fun daysFromToday(epochDay: Long): Long =
        ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.ofEpochDay(epochDay))

    fun properName(raw: String): String {
        if (raw.isBlank()) return raw
        return raw.trim().split(" ").filter { it.isNotBlank() }.joinToString(" ") { w ->
            w.lowercase(IT).replaceFirstChar { c -> c.titlecase(IT) }
        }
    }

    fun distance(km: Double): String =
        if (km < 1.0) "${Math.round(km * 1000)} m" else String.format(IT, "%.1f km", km)

    // Tempo relativo nudo: "ora", "5 h fa", "ieri", "3 giorni fa". null se ignoto.
    fun relativeUpdate(epochSeconds: Long): String? {
        if (epochSeconds <= 0L) return null
        val now = System.currentTimeMillis() / 1000
        val diff = now - epochSeconds
        return when {
            diff < 3600 -> "ora"
            diff < 86_400 -> "${diff / 3600} h fa"
            diff < 86_400 * 2 -> "ieri"
            else -> "${diff / 86_400} giorni fa"
        }
    }

    fun maskEmail(email: String): String {
        val at = email.indexOf('@')
        if (at <= 1) return email
        val name = email.substring(0, at)
        return "${name.first()}•••${name.last()}${email.substring(at)}"
    }

    fun maskPhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        if (digits.length < 4) return phone
        return "••• ••• ${digits.takeLast(3)}"
    }

    fun maskFiscalCode(cf: String): String {
        if (cf.length < 6) return cf
        return "${cf.take(3)}•••••${cf.takeLast(3)}"
    }
}
