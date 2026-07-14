package com.reelia.app.data.local.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.reelia.app.data.local.entity.SeasonEntity

@Dao
interface SeasonDao {
    @Upsert
    suspend fun upsertSeasons(seasons: List<SeasonEntity>)
}
