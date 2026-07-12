package com.timeline.app.data.local.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.timeline.app.data.local.entity.SeasonEntity

@Dao
interface SeasonDao {
    @Upsert
    suspend fun upsertSeasons(seasons: List<SeasonEntity>)
}
