package com.reelia.app.data.repository

import com.reelia.app.data.local.dao.GenreStat
import com.reelia.app.data.local.dao.WatchLogDao
import com.reelia.app.domain.model.MediaType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class BasicStats(
    val totalMinutesWatched: Int,
    val totalWatchedCount: Int,
)

/** One point on a weekly/monthly watch-time chart. Always backfilled to a fixed [count] of
 * periods — including ones with zero watched minutes — so the chart's axis stays stable instead
 * of only showing whichever periods happen to have data. */
data class TimeBucketEntry(
    val label: String,
    val minutesWatched: Int,
)

@Singleton
class StatsRepository @Inject constructor(
    private val watchLogDao: WatchLogDao,
) {
    fun getBasicStats(mediaType: MediaType? = null): Flow<BasicStats> =
        combine(watchLogDao.totalMinutesWatched(mediaType), watchLogDao.countEntries(mediaType)) { minutes, count ->
            BasicStats(totalMinutesWatched = minutes, totalWatchedCount = count)
        }

    /** [periodsAgo] shifts the visible window back by that many weeks — 0 shows the most recent
     * [count] weeks (oldest first, ending with the current week); 1 shifts the whole window back
     * one week, etc. */
    fun getWeeklyBreakdown(mediaType: MediaType? = null, periodsAgo: Int = 0, count: Int = 6): Flow<List<TimeBucketEntry>> =
        watchLogDao.getAllEntriesForBreakdown(mediaType).map { entries ->
            val zone = ZoneId.systemDefault()
            val weekFields = WeekFields.ISO
            val minutesByWeek = entries
                .groupingBy { it.watchedAt.weekKey(zone, weekFields) }
                .fold(0) { total, entry -> total + entry.runtimeMinutes }
            val today = LocalDate.now(zone)
            (count - 1 downTo 0).map { stepsBack ->
                val date = today.minusWeeks((periodsAgo + stepsBack).toLong())
                val week = date.get(weekFields.weekOfWeekBasedYear())
                TimeBucketEntry(
                    label = "S" + week.toString().padStart(2, '0'),
                    minutesWatched = minutesByWeek[date.weekKey(weekFields)] ?: 0,
                )
            }
        }

    /** Same idea as [getWeeklyBreakdown] but bucketed by calendar month. [TimeBucketEntry.label]
     * is a raw "YYYY-MM" key here — the UI layer reformats it into a locale-aware short month
     * name (e.g. "Jan 27"), matching this codebase's existing pattern of doing locale-aware
     * formatting at the ViewModel/UI layer rather than in the repository. */
    fun getMonthlyBreakdown(mediaType: MediaType? = null, periodsAgo: Int = 0, count: Int = 6): Flow<List<TimeBucketEntry>> =
        watchLogDao.getAllEntriesForBreakdown(mediaType).map { entries ->
            val zone = ZoneId.systemDefault()
            val minutesByMonth = entries
                .groupingBy { entry -> entry.watchedAt.atZone(zone).let { it.year * 12 + it.monthValue } }
                .fold(0) { total, entry -> total + entry.runtimeMinutes }
            val today = LocalDate.now(zone)
            (count - 1 downTo 0).map { stepsBack ->
                val date = today.minusMonths((periodsAgo + stepsBack).toLong())
                val key = date.year * 12 + date.monthValue
                TimeBucketEntry(
                    label = "${date.year}-" + date.monthValue.toString().padStart(2, '0'),
                    minutesWatched = minutesByMonth[key] ?: 0,
                )
            }
        }

    fun getGenreBreakdown(mediaType: MediaType? = null, limit: Int = 5): Flow<List<GenreStat>> =
        watchLogDao.getGenreBreakdown(mediaType, limit)

    /** Watch time bucketed by ISO day-of-week (1=Monday..7=Sunday), across the whole watch log
     * (not windowed like the weekly/monthly charts). [TimeBucketEntry.label] is the raw day
     * number as a string — the UI layer reformats it into a locale-aware short weekday name. */
    fun getWeekdayBreakdown(mediaType: MediaType? = null): Flow<List<TimeBucketEntry>> =
        watchLogDao.getAllEntriesForBreakdown(mediaType).map { entries ->
            val zone = ZoneId.systemDefault()
            val minutesByDay = entries
                .groupingBy { it.watchedAt.atZone(zone).dayOfWeek.value }
                .fold(0) { total, entry -> total + entry.runtimeMinutes }
            (1..7).map { day -> TimeBucketEntry(label = day.toString(), minutesWatched = minutesByDay[day] ?: 0) }
        }

    private fun Instant.weekKey(zone: ZoneId, weekFields: WeekFields): Pair<Int, Int> =
        atZone(zone).toLocalDate().weekKey(weekFields)

    private fun LocalDate.weekKey(weekFields: WeekFields): Pair<Int, Int> =
        get(weekFields.weekBasedYear()) to get(weekFields.weekOfWeekBasedYear())
}
