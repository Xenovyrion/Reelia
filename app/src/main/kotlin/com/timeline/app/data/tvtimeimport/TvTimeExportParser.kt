package com.timeline.app.data.tvtimeimport

import java.io.InputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.zip.ZipInputStream
import javax.inject.Inject

/**
 * Parses a TV Time GDPR data export (a ZIP of ~20 CSVs, or a single already-extracted CSV) into
 * [TvTimeImportData]. Files are identified by their header columns rather than by filename —
 * TV Time's own export filenames aren't documented/stable, but the two files this feature cares
 * about have header shapes found nowhere else in the export:
 *  - the "v2" tracking table (`most_recent_ep_watched` column) — current show-follow + per-episode
 *    watch history, keyed by TheTVDB show id.
 *  - the "v1"/legacy tracking table (`alpha_range_key` column) — the only source of watched-movie
 *    history (the v2 table has no movie rows at all).
 */
class TvTimeExportParser @Inject constructor() {

    private val sqlTimestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun parse(input: InputStream): TvTimeImportData {
        val bytes = input.readBytes()
        val csvTexts = if (isZip(bytes)) extractCsvEntries(bytes) else listOf(String(bytes, Charsets.UTF_8))

        var v2Rows: List<Map<String, String>>? = null
        var v1Rows: List<Map<String, String>>? = null
        for (csvText in csvTexts) {
            val rows = TvTimeCsvParser.parseToMaps(csvText)
            val header = rows.firstOrNull()?.keys ?: continue
            when {
                "most_recent_ep_watched" in header -> v2Rows = rows
                "alpha_range_key" in header -> v1Rows = rows
            }
        }

        return TvTimeImportData(
            shows = v2Rows?.let { extractShows(it) } ?: emptyList(),
            movies = v1Rows?.let { extractMovies(it) } ?: emptyList(),
        )
    }

    private fun isZip(bytes: ByteArray): Boolean =
        bytes.size >= 2 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte()

    private fun extractCsvEntries(bytes: ByteArray): List<String> {
        val texts = mutableListOf<String>()
        ZipInputStream(bytes.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(".csv", ignoreCase = true)) {
                    texts += zip.readBytes().toString(Charsets.UTF_8)
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return texts
    }

    private fun extractShows(rows: List<Map<String, String>>): List<TvTimeShowImport> {
        val seriesRows = rows.filter { it["key"]?.startsWith("user-series-") == true }

        val nameByTvdbId = seriesRows
            .asSequence()
            .mapNotNull { row ->
                val tvdbId = row["s_id"]?.toIntOrNull() ?: return@mapNotNull null
                val name = row["series_name"]?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                tvdbId to name
            }
            .toMap()

        // TV Time keeps a "user-series-*" row forever once a show is first followed — removing
        // it later only flips `is_followed` to false, the row (and any watch history recorded
        // while it was followed) stays in the export. Without this filter, a show the user
        // deliberately removed would silently come back on every import.
        val removedTvdbIds = seriesRows
            .asSequence()
            .filter { it["is_followed"] == "false" }
            .mapNotNull { it["s_id"]?.toIntOrNull() }
            .toSet()

        val watchedByShow = mutableMapOf<Int, MutableMap<Pair<Int, Int>, Instant>>()
        rows.asSequence()
            .filter { it["key"]?.startsWith("watch-episode-") == true }
            .forEach { row ->
                val tvdbId = row["s_id"]?.toIntOrNull() ?: return@forEach
                val season = row["s_no"]?.toIntOrNull() ?: return@forEach
                val episode = row["ep_no"]?.toIntOrNull() ?: return@forEach
                val watchedAt = parseSqlTimestamp(row["updated_at"]) ?: Instant.EPOCH
                val episodesForShow = watchedByShow.getOrPut(tvdbId) { mutableMapOf() }
                val key = season to episode
                val existing = episodesForShow[key]
                if (existing == null || watchedAt.isAfter(existing)) {
                    episodesForShow[key] = watchedAt
                }
            }

        return (nameByTvdbId.keys + watchedByShow.keys)
            .distinct()
            .filterNot { it in removedTvdbIds }
            .map { tvdbId ->
                TvTimeShowImport(
                    tvdbId = tvdbId,
                    name = nameByTvdbId[tvdbId] ?: "#$tvdbId",
                    watchedEpisodes = watchedByShow[tvdbId] ?: emptyMap(),
                )
            }
    }

    private fun extractMovies(rows: List<Map<String, String>>): List<TvTimeMovieImport> {
        val byNormalizedName = LinkedHashMap<String, TvTimeMovieImport>()
        for (row in rows) {
            if (row["entity_type"] != "movie") continue
            val name = row["movie_name"]?.takeIf { it.isNotBlank() } ?: continue
            val key = name.lowercase()
            val year = row["release_date"]?.take(4)?.toIntOrNull()
            when (row["type"]) {
                "watch" -> {
                    val existing = byNormalizedName[key]
                    if (existing == null || !existing.watched) {
                        val watchedAt = row["watch_date"]?.toLongOrNull()?.let(Instant::ofEpochSecond)
                        byNormalizedName[key] = TvTimeMovieImport(name, year, watched = true, watchedAt = watchedAt)
                    }
                }
                "follow", "towatch" -> {
                    if (byNormalizedName[key] == null) {
                        byNormalizedName[key] = TvTimeMovieImport(name, year, watched = false, watchedAt = null)
                    }
                }
            }
        }
        return byNormalizedName.values.toList()
    }

    private fun parseSqlTimestamp(raw: String?): Instant? {
        if (raw.isNullOrBlank()) return null
        return runCatching { LocalDateTime.parse(raw, sqlTimestampFormatter).toInstant(ZoneOffset.UTC) }.getOrNull()
    }
}
