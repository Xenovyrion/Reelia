package com.reelia.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.reelia.app.data.local.dao.EpisodeDao
import com.reelia.app.data.local.dao.GenreDao
import com.reelia.app.data.local.dao.MovieDao
import com.reelia.app.data.local.dao.SeasonDao
import com.reelia.app.data.local.dao.ShowDao
import com.reelia.app.data.local.dao.SyncOutboxDao
import com.reelia.app.data.local.dao.WatchLogDao
import com.reelia.app.data.local.entity.EpisodeEntity
import com.reelia.app.data.local.entity.GenreEntity
import com.reelia.app.data.local.entity.MovieGenreCrossRef
import com.reelia.app.data.local.entity.SeasonEntity
import com.reelia.app.data.local.entity.ShowGenreCrossRef
import com.reelia.app.data.local.entity.SyncOutboxEntity
import com.reelia.app.data.local.entity.TrackedMovieEntity
import com.reelia.app.data.local.entity.TrackedShowEntity
import com.reelia.app.data.local.entity.WatchLogEntryEntity

@Database(
    entities = [
        TrackedShowEntity::class,
        SeasonEntity::class,
        EpisodeEntity::class,
        TrackedMovieEntity::class,
        GenreEntity::class,
        ShowGenreCrossRef::class,
        MovieGenreCrossRef::class,
        WatchLogEntryEntity::class,
        SyncOutboxEntity::class,
    ],
    version = 7,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun showDao(): ShowDao
    abstract fun seasonDao(): SeasonDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun movieDao(): MovieDao
    abstract fun genreDao(): GenreDao
    abstract fun watchLogDao(): WatchLogDao
    abstract fun syncOutboxDao(): SyncOutboxDao

    companion object {
        const val DATABASE_NAME = "timeline.db"
    }
}
