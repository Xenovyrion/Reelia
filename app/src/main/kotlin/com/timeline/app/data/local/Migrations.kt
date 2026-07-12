package com.timeline.app.data.local

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
