package com.reelia.app.ui.widget

import android.content.Context
import com.reelia.app.data.remote.tmdb.TmdbImageUrlBuilder
import com.reelia.app.data.repository.MovieRepository
import com.reelia.app.data.repository.ShowRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * [RefreshAction] is instantiated reflectively by Glance itself (via a no-arg constructor) when
 * its click action fires, so it can't receive constructor injection the way
 * [ReeliaWidgetReceiver] does (that one's a BroadcastReceiver, a Hilt-supported injection target,
 * with `@AndroidEntryPoint` field injection). This entry point is the standard way to reach into
 * the Hilt graph from a class Hilt doesn't otherwise know how to construct.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReeliaWidgetEntryPoint {
    fun showRepository(): ShowRepository
    fun movieRepository(): MovieRepository
    fun tmdbImageUrlBuilder(): TmdbImageUrlBuilder
}

fun createReeliaWidget(context: Context): ReeliaWidget {
    val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, ReeliaWidgetEntryPoint::class.java)
    return ReeliaWidget(entryPoint.showRepository(), entryPoint.movieRepository(), entryPoint.tmdbImageUrlBuilder())
}
