package com.timeline.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.timeline.app.data.local.AppDatabase
import com.timeline.app.data.local.dao.EpisodeDao
import com.timeline.app.data.local.dao.GenreDao
import com.timeline.app.data.local.dao.MovieDao
import com.timeline.app.data.local.dao.SeasonDao
import com.timeline.app.data.local.dao.ShowDao
import com.timeline.app.data.local.dao.WatchLogDao
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
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore
}
