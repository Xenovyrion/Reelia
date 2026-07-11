package com.timeline.app.domain.model

enum class WatchStatus {
    WATCHING,
    PLAN_TO_WATCH,
    COMPLETED,
    DROPPED,
    ON_HOLD,
}

fun WatchStatus.displayLabel(): String = when (this) {
    WatchStatus.WATCHING -> "En cours"
    WatchStatus.PLAN_TO_WATCH -> "À voir"
    WatchStatus.COMPLETED -> "Terminée"
    WatchStatus.DROPPED -> "Abandonnée"
    WatchStatus.ON_HOLD -> "En pause"
}
