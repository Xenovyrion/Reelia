package com.reelia.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.reelia.app.data.local.entity.NotifiedReminderEntity

@Dao
interface NotifiedReminderDao {
    @Query("SELECT tmdbId, mediaType, offsetDays, targetDate FROM notified_reminders")
    suspend fun getAllKeys(): List<NotifiedReminderKey>

    @Insert
    suspend fun insertAll(entries: List<NotifiedReminderEntity>)
}
