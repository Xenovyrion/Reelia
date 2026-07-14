package com.reelia.app.data.releasenotes

enum class ReleaseNoteCategory {
    FEATURE,
    FIX,
    IMPROVEMENT,
    OTHER,
}

data class ReleaseNoteItem(val category: ReleaseNoteCategory, val text: String)

data class ReleaseNoteVersion(val version: String, val dateLabel: String, val items: List<ReleaseNoteItem>)
