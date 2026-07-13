package com.timeline.app.data.tvtimeimport

/**
 * Minimal RFC4180-style CSV parser (quoted fields, `""` escaping, quoted fields that may contain
 * commas or newlines). Hand-rolled instead of a dependency — TV Time's GDPR export is a handful
 * of files a few MB each, well within what a straightforward char-by-char parser handles.
 */
object TvTimeCsvParser {

    /** Parses [csvText] into a list of rows, each a header-name-to-value map. The first line is
     * treated as the header row. */
    fun parseToMaps(csvText: String): List<Map<String, String>> {
        val rows = parseRows(csvText)
        if (rows.isEmpty()) return emptyList()
        val header = rows.first()
        return rows.drop(1).map { row ->
            header.indices.associate { i -> header[i] to (row.getOrNull(i) ?: "") }
        }
    }

    private fun parseRows(csvText: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0
        val n = csvText.length

        fun endField() {
            currentRow.add(field.toString())
            field.clear()
        }

        fun endRow() {
            endField()
            rows.add(currentRow.toList())
            currentRow.clear()
        }

        while (i < n) {
            val c = csvText[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < n && csvText[i + 1] == '"') {
                        field.append('"')
                        i += 2
                        continue
                    }
                    inQuotes = false
                    i++
                    continue
                }
                field.append(c)
                i++
                continue
            }
            when (c) {
                '"' -> {
                    inQuotes = true
                    i++
                }
                ',' -> {
                    endField()
                    i++
                }
                '\r' -> {
                    i++
                }
                '\n' -> {
                    endRow()
                    i++
                }
                else -> {
                    field.append(c)
                    i++
                }
            }
        }
        // Trailing field/row (file may or may not end with a newline).
        if (field.isNotEmpty() || currentRow.isNotEmpty()) {
            endRow()
        }
        return rows.filter { row -> row.size > 1 || row.firstOrNull()?.isNotBlank() == true }
    }
}
