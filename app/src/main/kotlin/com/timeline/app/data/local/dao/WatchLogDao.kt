package com.timeline.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.timeline.app.data.local.entity.WatchLogEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchLogDao {
    @Insert
    suspend fun insert(entry: WatchLogEntryEntity)

    @Query("SELECT COUNT(*) FROM watch_log")
    fun countEntries(): Flow<Int>

    @Query("SELECT COALESCE(SUM(runtimeMinutes), 0) FROM watch_log")
    fun totalMinutesWatched(): Flow<Int>
}
