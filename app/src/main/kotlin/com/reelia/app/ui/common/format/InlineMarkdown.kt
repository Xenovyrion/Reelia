package com.reelia.app.ui.common.format

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

private val linkRegex = Regex("\\[([^]]+)]\\([^)]+\\)")
private val boldOrItalicRegex = Regex("\\*\\*(.+?)\\*\\*|\\*(.+?)\\*")

/** Renders the small subset of inline Markdown used by docs/guide/{fr,en}.md — `**bold**`,
 * `*italic*`, and `[text](url)` links (shown as plain text, not clickable, since the guide has
 * only a couple of these and a real tappable-link renderer isn't worth the complexity here). */
fun String.parseInlineMarkdown(): AnnotatedString {
    val withLinksStripped = linkRegex.replace(this) { it.groupValues[1] }
    return buildAnnotatedString {
        var lastIndex = 0
        for (match in boldOrItalicRegex.findAll(withLinksStripped)) {
            append(withLinksStripped.substring(lastIndex, match.range.first))
            val bold = match.groupValues[1]
            if (bold.isNotEmpty()) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(bold) }
            } else {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(match.groupValues[2]) }
            }
            lastIndex = match.range.last + 1
        }
        append(withLinksStripped.substring(lastIndex))
    }
}
