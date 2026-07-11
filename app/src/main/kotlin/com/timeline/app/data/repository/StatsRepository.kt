package com.timeline.app.data.repository

import com.timeline.app.data.local.dao.WatchLogDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class BasicStats(
    val totalMinutesWatched: Int,
    val totalWatchedCount: Int,
)

@Singleton
class StatsRepository @Inject constructor(
    private val watchLogDao: WatchLogDao,
) {
    fun getBasicStats(): Flow<BasicStats> =
        combine(watchLogDao.totalMinutesWatched(), watchLogDao.countEntries()) { minutes, count ->
            BasicStats(totalMinutesWatched = minutes, totalWatchedCount = count)
        }
}
