package com.reelia.app.ui.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.reelia.app.MainActivity
import com.reelia.app.R
import com.reelia.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.reelia.app.data.repository.MovieRepository
import com.reelia.app.data.repository.ShowRepository
import com.reelia.app.domain.model.MediaType
import com.reelia.app.ui.common.model.buildUpcomingMovieItems
import com.reelia.app.ui.common.model.buildUpcomingShowItems
import kotlinx.coroutines.flow.first

private val WidgetBackground = ColorProvider(Color(0xFF13111C))
private val WidgetSurface = ColorProvider(Color(0xFF1D1B29))
private val WidgetAccent = ColorProvider(Color(0xFF33E4C6))
private val WidgetOnSurface = ColorProvider(Color(0xFFF4F2F9))
private val WidgetOnSurfaceVariant = ColorProvider(Color(0xFF948FA8))

private const val MAX_ITEMS = 5
private const val POSTER_WIDTH_PX = 96
private const val POSTER_HEIGHT_PX = 144

/** A shared shape for shows and movies, so the widget can merge and sort them into one
 * chronological list without caring which kind of title each row is until it's tapped. */
private data class WidgetItem(
    val mediaType: MediaType,
    val tmdbId: Int,
    val title: String,
    val posterUrl: String?,
    val daysUntil: Long,
    val sortKey: String,
)

/**
 * Compact "what's coming up" home screen widget — reuses the exact same upcoming-item logic as
 * the Home screen's "À venir" section (`buildUpcomingShowItems`/`buildUpcomingMovieItems`) and
 * the exact same tap-to-open-detail mechanism the release-reminder notifications use
 * (`MainActivity.EXTRA_MEDIA_TYPE`/`EXTRA_TMDB_ID` -> `PendingNotificationTarget`), so there's
 * only one deep-link path into a detail screen to reason about, not two.
 *
 * Glance has its own composition system (`androidx.glance.*`), incompatible with regular Compose
 * UI (`androidx.compose.*`) beyond sharing the `@Composable` annotation and a few primitive
 * types like `Color` — none of the app's Material3 components or `MaterialTheme` can be reused
 * here, hence the hand-rolled color constants above (matching `ui/theme/Color.kt`'s values).
 */
class ReeliaWidget(
    private val showRepository: ShowRepository,
    private val movieRepository: MovieRepository,
    private val imageUrlBuilder: TmdbImageUrlBuilder,
) : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val items = loadItems()
        val posters = loadPosters(context, items)
        provideContent {
            WidgetContent(items, posters)
        }
    }

    private suspend fun loadItems(): List<WidgetItem> {
        val shows = showRepository.getAllShows().first()
        val movies = movieRepository.getAllMovies().first()
        val upcomingShows = buildUpcomingShowItems(shows, imageUrlBuilder).map {
            WidgetItem(MediaType.TV, it.showId, it.showTitle, it.posterUrl, it.daysUntil, it.airDate)
        }
        val upcomingMovies = buildUpcomingMovieItems(movies, imageUrlBuilder).map {
            WidgetItem(MediaType.MOVIE, it.movieId, it.title, it.posterUrl, it.daysUntil, it.releaseDate)
        }
        return (upcomingShows + upcomingMovies).sortedBy { it.sortKey }.take(MAX_ITEMS)
    }

    /** Best-effort poster thumbnails via the app's shared Coil loader (Glance can't use Coil's
     * `AsyncImage` composable directly — different composition system — so this fetches a real
     * `Bitmap` up front instead). A failed or slow fetch degrades that one row to no image rather
     * than failing the whole widget. */
    private suspend fun loadPosters(context: Context, items: List<WidgetItem>): Map<Int, Bitmap> {
        val loader = Coil.imageLoader(context)
        val result = mutableMapOf<Int, Bitmap>()
        items.forEach { item ->
            val url = item.posterUrl ?: return@forEach
            val request = ImageRequest.Builder(context)
                .data(url)
                .size(POSTER_WIDTH_PX, POSTER_HEIGHT_PX)
                .build()
            val bitmap = (loader.execute(request) as? SuccessResult)?.drawable
                ?.toBitmap(POSTER_WIDTH_PX, POSTER_HEIGHT_PX)
            if (bitmap != null) result[item.tmdbId] = bitmap
        }
        return result
    }

    @Composable
    private fun WidgetContent(items: List<WidgetItem>, posters: Map<Int, Bitmap>) {
        val context = LocalContext.current
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WidgetBackground)
                .padding(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.fillMaxWidth(),
            ) {
                Text(
                    text = context.getString(R.string.widget_title),
                    style = TextStyle(color = WidgetOnSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "⟳",
                    style = TextStyle(color = WidgetOnSurfaceVariant, fontSize = 16.sp),
                    modifier = GlanceModifier
                        .clickable(actionRunCallback<RefreshAction>())
                        .padding(4.dp),
                )
            }
            Spacer(modifier = GlanceModifier.height(8.dp))

            if (items.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = context.getString(R.string.widget_empty_state),
                        style = TextStyle(color = WidgetOnSurfaceVariant, fontSize = 13.sp),
                    )
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(items, itemId = { it.tmdbId.toLong() }) { item ->
                        WidgetRow(context, item, posters[item.tmdbId])
                        Spacer(modifier = GlanceModifier.height(6.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetRow(context: Context, item: WidgetItem, poster: Bitmap?) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(WidgetSurface)
                .cornerRadius(10.dp)
                .padding(6.dp)
                .clickable(actionStartActivity(detailIntent(context, item))),
        ) {
            if (poster != null) {
                Image(
                    provider = ImageProvider(poster),
                    contentDescription = null,
                    modifier = GlanceModifier.size(width = 28.dp, height = 42.dp).cornerRadius(4.dp),
                )
            } else {
                Box(modifier = GlanceModifier.size(width = 28.dp, height = 42.dp).background(WidgetBackground).cornerRadius(4.dp)) {}
            }
            Spacer(modifier = GlanceModifier.width(8.dp))
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = item.title,
                    maxLines = 1,
                    style = TextStyle(color = WidgetOnSurface, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                )
                Text(
                    text = countdownLabel(context, item.daysUntil),
                    maxLines = 1,
                    style = TextStyle(color = WidgetAccent, fontSize = 11.sp),
                )
            }
        }
    }

    /** Mirrors UpcomingCountdownBadge's wording — that component can't be reused directly since
     * it's a regular Compose composable built on Material3, incompatible with Glance's own
     * composition system, so this is a deliberate small duplication rather than an oversight. */
    private fun countdownLabel(context: Context, daysUntil: Long): String = when {
        daysUntil < 0 -> context.getString(R.string.days_until_aired)
        daysUntil == 0L -> context.getString(R.string.days_until_today)
        daysUntil == 1L -> context.getString(R.string.days_until_tomorrow)
        else -> context.getString(R.string.days_until_in_days, daysUntil.toInt())
    }

    private fun detailIntent(context: Context, item: WidgetItem): Intent =
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_MEDIA_TYPE, item.mediaType.name)
            putExtra(MainActivity.EXTRA_TMDB_ID, item.tmdbId)
        }
}
