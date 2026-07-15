package com.reelia.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.reelia.app.domain.model.MediaType
import com.reelia.app.ui.guide.GuideScreen
import com.reelia.app.ui.home.HomeScreen
import com.reelia.app.ui.library.LibraryScreen
import com.reelia.app.ui.moviedetail.MovieDetailScreen
import com.reelia.app.ui.persondetail.PersonDetailScreen
import com.reelia.app.ui.preview.MoviePreviewScreen
import com.reelia.app.ui.preview.ShowPreviewScreen
import com.reelia.app.ui.profile.ProfileScreen
import com.reelia.app.ui.search.SearchScreen
import com.reelia.app.ui.releasenotes.ReleaseNotesScreen
import com.reelia.app.ui.showdetail.ShowDetailScreen
import com.reelia.app.ui.statsdetail.StatsDetailScreen
import com.reelia.app.ui.tvtimeimport.TvTimeImportScreen

private fun navigateToItem(navController: NavHostController, mediaType: MediaType, id: Int) {
    when (mediaType) {
        MediaType.TV -> navController.navigate(Routes.showDetail(id))
        MediaType.MOVIE -> navController.navigate(Routes.movieDetail(id))
    }
}

/** For TMDB items not necessarily already in the library (search results, Home's discovery
 * rows) — routes through the preview screen instead of assuming a Room entity exists. */
private fun navigateToPreview(navController: NavHostController, mediaType: MediaType, id: Int) {
    when (mediaType) {
        MediaType.TV -> navController.navigate(Routes.showPreview(id))
        MediaType.MOVIE -> navController.navigate(Routes.moviePreview(id))
    }
}

/** After adding a title (from the search results list or its preview screen), lands the user
 * back on whatever screen was open before Search — not on Search or its preview screen, which
 * would otherwise be left sitting in the back stack. Explicit pop-then-navigate rather than a
 * single navigate(...) { popUpTo(...) } call, so the preview screen (if any) is unconditionally
 * gone before the destination is pushed, regardless of exactly which entry started the add. */
private fun navigateToItemAfterAdd(navController: NavHostController, mediaType: MediaType, id: Int) {
    navController.popBackStack(route = Routes.SEARCH, inclusive = true)
    navController.navigate(
        when (mediaType) {
            MediaType.TV -> Routes.showDetail(id)
            MediaType.MOVIE -> Routes.movieDetail(id)
        },
    )
}

@Composable
fun TimeLineNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier,
        enterTransition = { fadeIn() + slideInHorizontally(initialOffsetX = { it / 4 }) },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() + slideOutHorizontally(targetOffsetX = { it / 4 }) },
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onShowClick = { navController.navigate(Routes.showDetail(it)) },
                onDiscoverItemClick = { mediaType, id -> navigateToPreview(navController, mediaType, id) },
                onSearchClick = { navController.navigate(Routes.search()) },
            )
        }
        composable(Routes.SERIES) {
            LibraryScreen(
                fixedMediaType = MediaType.TV,
                onItemClick = { mediaType, id -> navigateToItem(navController, mediaType, id) },
                onSearchClick = { navController.navigate(Routes.search(MediaType.TV)) },
            )
        }
        composable(Routes.FILMS) {
            LibraryScreen(
                fixedMediaType = MediaType.MOVIE,
                onItemClick = { mediaType, id -> navigateToItem(navController, mediaType, id) },
                onSearchClick = { navController.navigate(Routes.search(MediaType.MOVIE)) },
            )
        }
        composable(
            route = Routes.SEARCH,
            arguments = listOf(
                navArgument("mediaType") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            SearchScreen(
                onItemClick = { mediaType, id -> navigateToPreview(navController, mediaType, id) },
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                onImportClick = { navController.navigate(Routes.TV_TIME_IMPORT) },
                onReleaseNotesClick = { navController.navigate(Routes.RELEASE_NOTES) },
                onGuideClick = { navController.navigate(Routes.GUIDE) },
                onStatsDetailClick = { filterType, filterId, filterLabel ->
                    navController.navigate(Routes.statsDetail(filterType, filterId, filterLabel))
                },
            )
        }
        composable(Routes.TV_TIME_IMPORT) {
            TvTimeImportScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.RELEASE_NOTES) {
            ReleaseNotesScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.GUIDE) {
            GuideScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.STATS_DETAIL,
            arguments = listOf(
                navArgument("filterType") { type = NavType.StringType },
                navArgument("filterId") { type = NavType.StringType },
                navArgument("filterLabel") { type = NavType.StringType },
            ),
        ) {
            StatsDetailScreen(
                onBack = { navController.popBackStack() },
                onItemClick = { mediaType, id -> navigateToItem(navController, mediaType, id) },
            )
        }

        composable(
            route = Routes.SHOW_DETAIL,
            arguments = listOf(navArgument("showId") { type = NavType.IntType }),
        ) {
            ShowDetailScreen(
                onBack = { navController.popBackStack() },
                onPersonClick = { navController.navigate(Routes.personDetail(it)) },
            )
        }
        composable(
            route = Routes.MOVIE_DETAIL,
            arguments = listOf(navArgument("movieId") { type = NavType.IntType }),
        ) {
            MovieDetailScreen(
                onBack = { navController.popBackStack() },
                onPersonClick = { navController.navigate(Routes.personDetail(it)) },
            )
        }
        composable(
            route = Routes.SHOW_PREVIEW,
            arguments = listOf(navArgument("tmdbId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val tmdbId = checkNotNull(backStackEntry.arguments?.getInt("tmdbId"))
            ShowPreviewScreen(
                onBack = { navController.popBackStack() },
                onAdded = { navigateToItemAfterAdd(navController, MediaType.TV, tmdbId) },
                onPersonClick = { navController.navigate(Routes.personDetail(it)) },
            )
        }
        composable(
            route = Routes.MOVIE_PREVIEW,
            arguments = listOf(navArgument("tmdbId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val tmdbId = checkNotNull(backStackEntry.arguments?.getInt("tmdbId"))
            MoviePreviewScreen(
                onBack = { navController.popBackStack() },
                onAdded = { navigateToItemAfterAdd(navController, MediaType.MOVIE, tmdbId) },
                onPersonClick = { navController.navigate(Routes.personDetail(it)) },
            )
        }
        composable(
            route = Routes.PERSON_DETAIL,
            arguments = listOf(navArgument("personId") { type = NavType.IntType }),
        ) {
            PersonDetailScreen(
                onBack = { navController.popBackStack() },
                onShowClick = { navController.navigate(Routes.showPreview(it)) },
                onMovieClick = { navController.navigate(Routes.moviePreview(it)) },
            )
        }
    }
}
