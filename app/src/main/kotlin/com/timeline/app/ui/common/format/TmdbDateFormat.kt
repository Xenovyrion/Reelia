package com.timeline.app.ui.common.format

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** TMDB dates are ISO "yyyy-MM-dd" strings; cards should show only the release year. */
fun String?.toYearOrNull(): String? =
    this?.takeIf { it.length >= 4 && it.take(4).all(Char::isDigit) }?.take(4)

/** Formats a TMDB ISO "yyyy-MM-dd" date string using the device's current locale (e.g.
 * "18 janvier 1955" in French, "January 18, 1955" in English) instead of showing the raw
 * ISO string — falls back to the raw string if it can't be parsed. */
fun String?.toFormattedDateOrNull(includeWeekday: Boolean = false): String? {
    if (this == null) return null
    val pattern = if (includeWeekday) "EEEE d MMMM yyyy" else "d MMMM yyyy"
    return try {
        LocalDate.parse(this).format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    } catch (e: Exception) {
        this
    }
}
