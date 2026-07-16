package com.reelia.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds the isFavorite flag introduced in Pass 8. */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tracked_shows ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE tracked_movies ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
    }
}

/** Adds lastModifiedAt (for last-write-wins sync) and the sync outbox table. */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tracked_shows ADD COLUMN lastModifiedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE tracked_movies ADD COLUMN lastModifiedAt INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS sync_outbox (
                tmdbId INTEGER NOT NULL,
                mediaType TEXT NOT NULL,
                updatedAt INTEGER NOT NULL,
                PRIMARY KEY(tmdbId, mediaType)
            )
            """.trimIndent(),
        )
    }
}

/** Adds syncId (client UUID) to watch_log for cross-device sync + remote-listener dedup.
 * Pre-existing rows get a blank syncId and are never retroactively synced — only entries
 * logged after this migration are pushed to Firestore. */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE watch_log ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
    }
}

/** Adds contentRating (age/content classification, e.g. "16", "PG-13"). Backfilled lazily —
 * existing rows get a null rating until the next time their show/movie is re-fetched from TMDB
 * (add, or a Firestore-triggered refresh), not eagerly on migration. */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tracked_shows ADD COLUMN contentRating TEXT")
        db.execSQL("ALTER TABLE tracked_movies ADD COLUMN contentRating TEXT")
    }
}

/** Backfills watch_log rows whose runtimeMinutes fell back to 0 (see RuntimeDefaults) with a
 * reasonable estimate instead — no genuine watch event has a true 0-minute duration, so this
 * condition unambiguously identifies rows affected by that bug. */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE watch_log SET runtimeMinutes = 45 WHERE runtimeMinutes = 0 AND mediaType = 'TV'")
        db.execSQL("UPDATE watch_log SET runtimeMinutes = 100 WHERE runtimeMinutes = 0 AND mediaType = 'MOVIE'")
    }
}
