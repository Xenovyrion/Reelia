package com.reelia.app.data.releasenotes

/**
 * Parses the small Markdown convention used by docs/release-notes/{en,fr}.md:
 *
 * ```
 * ## 0.7.0 — 13 juillet 2026
 * ### ✨ Nouveautés
 * - item
 * ### 🐛 Corrections
 * - item
 * ```
 *
 * Categories are recognized by their leading emoji rather than the (localized) heading text,
 * so the same parser works for both language files without needing to know which language it's
 * reading.
 */
object ReleaseNotesParser {
    private val versionHeaderRegex = Regex("^##\\s+(\\S+)\\s*(?:[—-]\\s*(.*))?$")

    fun parse(markdown: String): List<ReleaseNoteVersion> {
        val versions = mutableListOf<ReleaseNoteVersion>()
        var currentVersion: String? = null
        var currentDate = ""
        var currentCategory = ReleaseNoteCategory.OTHER
        var currentItems = mutableListOf<ReleaseNoteItem>()

        fun flush() {
            val version = currentVersion ?: return
            versions += ReleaseNoteVersion(version, currentDate, currentItems.toList())
        }

        markdown.lineSequence().forEach { raw ->
            val line = raw.trim()
            when {
                line.startsWith("## ") -> {
                    flush()
                    currentItems = mutableListOf()
                    val match = versionHeaderRegex.find(line)
                    currentVersion = match?.groupValues?.get(1) ?: line.removePrefix("## ").trim()
                    currentDate = match?.groupValues?.get(2)?.trim().orEmpty()
                    currentCategory = ReleaseNoteCategory.OTHER
                }
                line.startsWith("### ") -> {
                    val label = line.removePrefix("### ")
                    currentCategory = when {
                        label.startsWith("✨") -> ReleaseNoteCategory.FEATURE
                        label.startsWith("🐛") -> ReleaseNoteCategory.FIX
                        label.startsWith("🔧") -> ReleaseNoteCategory.IMPROVEMENT
                        else -> ReleaseNoteCategory.OTHER
                    }
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    if (currentVersion != null) {
                        currentItems.add(ReleaseNoteItem(currentCategory, line.drop(2).trim()))
                    }
                }
                else -> Unit
            }
        }
        flush()
        return versions
    }
}
