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
                onItemAdded = { mediaType, id ->
                    navController.navigate(
                        when (mediaType) {
                            MediaType.TV -> Routes.showDetail(id)
                            MediaType.MOVIE -> Routes.movieDetail(id)
                        },
                    ) {
                        popUpTo(Routes.SEARCH) { inclusive = true }
                    }
                },
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
                onAdded = {
                    // Skips back past Search entirely (a no-op if Search isn't in the back stack,
                    // e.g. reached from Home's discovery rows) — pressing back after adding a
                    // title should land on whatever screen was open before Search, typically the
                    // Library tab, not back on the search results.
                    navController.navigate(Routes.showDetail(tmdbId)) {
                        popUpTo(Routes.SEARCH) { inclusive = true }
                    }
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
                    navController.navigate(Routes.movieDetail(tmdbId)) {
                        popUpTo(Routes.SEARCH) { inclusive = true }
                    }
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
