package com.reelia.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.reelia.app.data.local.AppDatabase
import com.reelia.app.data.local.MIGRATION_4_5
import com.reelia.app.data.local.MIGRATION_5_6
import com.reelia.app.data.local.MIGRATION_6_7
import com.reelia.app.data.local.MIGRATION_7_8
import com.reelia.app.data.local.MIGRATION_8_9
import com.reelia.app.data.local.dao.EpisodeDao
import com.reelia.app.data.local.dao.GenreDao
import com.reelia.app.data.local.dao.MovieDao
import com.reelia.app.data.local.dao.SeasonDao
import com.reelia.app.data.local.dao.ShowDao
import com.reelia.app.data.local.dao.SyncOutboxDao
import com.reelia.app.data.local.dao.WatchLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "timeline_prefs")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideShowDao(db: AppDatabase): ShowDao = db.showDao()

    @Provides
    fun provideSeasonDao(db: AppDatabase): SeasonDao = db.seasonDao()

    @Provides
    fun provideEpisodeDao(db: AppDatabase): EpisodeDao = db.episodeDao()

    @Provides
    fun provideMovieDao(db: AppDatabase): MovieDao = db.movieDao()

    @Provides
    fun provideGenreDao(db: AppDatabase): GenreDao = db.genreDao()

    @Provides
    fun provideWatchLogDao(db: AppDatabase): WatchLogDao = db.watchLogDao()

    @Provides
    fun provideSyncOutboxDao(db: AppDatabase): SyncOutboxDao = db.syncOutboxDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore
}
