package com.timeline.app.ui.common.format

/** TMDB dates are ISO "yyyy-MM-dd" strings; cards should show only the release year. */
fun String?.toYearOrNull(): String? =
    this?.takeIf { it.length >= 4 && it.take(4).all(Char::isDigit) }?.take(4)
