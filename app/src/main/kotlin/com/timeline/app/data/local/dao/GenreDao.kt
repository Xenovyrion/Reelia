package com.timeline.app.data.local.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.timeline.app.data.local.entity.GenreEntity
import com.timeline.app.data.local.entity.MovieGenreCrossRef
import com.timeline.app.data.local.entity.ShowGenreCrossRef

@Dao
interface GenreDao {
    @Upsert
    suspend fun upsertGenres(genres: List<GenreEntity>)

    @Upsert
    suspend fun upsertShowCrossRefs(crossRefs: List<ShowGenreCrossRef>)

    @Upsert
    suspend fun upsertMovieCrossRefs(crossRefs: List<MovieGenreCrossRef>)
}
