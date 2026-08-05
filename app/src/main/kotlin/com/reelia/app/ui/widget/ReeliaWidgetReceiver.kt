package com.reelia.app.ui.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.reelia.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.reelia.app.data.repository.MovieRepository
import com.reelia.app.data.repository.ShowRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Entry point Android calls into for widget updates/lifecycle (see the `<receiver>` block in
 * AndroidManifest.xml). A plain `BroadcastReceiver` subclass, so unlike [RefreshAction] it can use
 * standard Hilt field injection via `@AndroidEntryPoint`.
 */
@AndroidEntryPoint
class ReeliaWidgetReceiver : GlanceAppWidgetReceiver() {

    @Inject lateinit var showRepository: ShowRepository
    @Inject lateinit var movieRepository: MovieRepository
    @Inject lateinit var imageUrlBuilder: TmdbImageUrlBuilder

    override val glanceAppWidget: GlanceAppWidget
        get() = ReeliaWidget(showRepository, movieRepository, imageUrlBuilder)
}
