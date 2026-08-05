package com.reelia.app.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Guards against the exact bug that caused an earlier destructive-migration incident (see
 * AppDatabase's `APP_DATABASE_VERSION` doc comment): a schema version bump that ships without a
 * matching [androidx.room.migration.Migration] registered in [ALL_MIGRATIONS]. With no migration
 * covering the jump, `fallbackToDestructiveMigration()` (see DatabaseModule) silently wipes every
 * user's local database on their next update instead of upgrading it in place.
 *
 * Pure JVM — reads only `startVersion`/`endVersion` off each [androidx.room.migration.Migration],
 * never touches SQLite, so no Robolectric/instrumentation is needed to run this.
 */
class AppDatabaseMigrationCoverageTest {

    @Test
    fun `migrations form an unbroken chain up to the current schema version`() {
        val sorted = ALL_MIGRATIONS.sortedBy { it.startVersion }
        for (i in 1 until sorted.size) {
            assertEquals(
                "Gap: migration ${sorted[i - 1].startVersion}->${sorted[i - 1].endVersion} is not " +
                    "immediately followed by one starting at ${sorted[i - 1].endVersion} " +
                    "(found ${sorted[i].startVersion}->${sorted[i].endVersion} instead)",
                sorted[i - 1].endVersion,
                sorted[i].startVersion,
            )
        }
        assertEquals(
            "AppDatabase.version (APP_DATABASE_VERSION) was bumped without a matching migration " +
                "added to ALL_MIGRATIONS — this would destructively wipe local data on update",
            APP_DATABASE_VERSION,
            sorted.last().endVersion,
        )
    }

    @Test
    fun `no two migrations share the same start version`() {
        val startVersions = ALL_MIGRATIONS.map { it.startVersion }
        assertEquals(
            "Duplicate migration start version(s) in ALL_MIGRATIONS: $startVersions",
            startVersions.size,
            startVersions.toSet().size,
        )
    }
}
