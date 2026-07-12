package com.timeline.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.timeline.app.data.local.dao.EpisodeDao
import com.timeline.app.data.local.dao.GenreDao
import com.timeline.app.data.local.dao.MovieDao
import com.timeline.app.data.local.dao.SeasonDao
import com.timeline.app.data.local.dao.ShowDao
import com.timeline.app.data.local.dao.WatchLogDao
import com.timeline.app.data.local.entity.EpisodeEntity
import com.timeline.app.data.local.entity.GenreEntity
import com.timeline.app.data.local.entity.MovieGenreCrossRef
import com.timeline.app.data.local.entity.SeasonEntity
import com.timeline.app.data.local.entity.ShowGenreCrossRef
import com.timeline.app.data.local.entity.TrackedMovieEntity
import com.timeline.app.data.local.entity.TrackedShowEntity
import com.timeline.app.data.local.entity.WatchLogEntryEntity

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
    ],
    version = 4,
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

    companion object {
        const val DATABASE_NAME = "timeline.db"
    }
}
