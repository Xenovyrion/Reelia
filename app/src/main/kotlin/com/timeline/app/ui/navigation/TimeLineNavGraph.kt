package com.timeline.app.ui.navigation

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
import com.timeline.app.domain.model.MediaType
import com.timeline.app.ui.home.HomeScreen
import com.timeline.app.ui.library.LibraryScreen
import com.timeline.app.ui.moviedetail.MovieDetailScreen
import com.timeline.app.ui.persondetail.PersonDetailScreen
import com.timeline.app.ui.preview.MoviePreviewScreen
import com.timeline.app.ui.preview.ShowPreviewScreen
import com.timeline.app.ui.profile.ProfileScreen
import com.timeline.app.ui.search.SearchScreen
import com.timeline.app.ui.releasenotes.ReleaseNotesScreen
import com.timeline.app.ui.showdetail.ShowDetailScreen
import com.timeline.app.ui.statsdetail.StatsDetailScreen
import com.timeline.app.ui.tvtimeimport.TvTimeImportScreen

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
                onAdded = {
                    navController.popBackStack()
                    navController.navigate(Routes.showDetail(tmdbId))
                },
            )
        }
        composable(
            route = Routes.MOVIE_PREVIEW,
            arguments = listOf(navArgument("tmdbId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val tmdbId = checkNotNull(backStackEntry.arguments?.getInt("tmdbId"))
            MoviePreviewScreen(
                onBack = { navController.popBackStack() },
                onAdded = {
                    navController.popBackStack()
                    navController.navigate(Routes.movieDetail(tmdbId))
                },
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
