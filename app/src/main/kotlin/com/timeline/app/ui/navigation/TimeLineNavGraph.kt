package com.timeline.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.timeline.app.domain.model.MediaType
import com.timeline.app.ui.films.FilmsScreen
import com.timeline.app.ui.moviedetail.MovieDetailScreen
import com.timeline.app.ui.persondetail.PersonDetailScreen
import com.timeline.app.ui.preview.MoviePreviewScreen
import com.timeline.app.ui.preview.ShowPreviewScreen
import com.timeline.app.ui.search.SearchScreen
import com.timeline.app.ui.series.SeriesScreen
import com.timeline.app.ui.settings.SettingsScreen
import com.timeline.app.ui.showdetail.ShowDetailScreen
import com.timeline.app.ui.stats.StatsScreen

@Composable
fun TimeLineNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Routes.SERIES, modifier = modifier) {
        composable(Routes.SERIES) {
            SeriesScreen(onShowClick = { navController.navigate(Routes.showDetail(it)) })
        }
        composable(Routes.FILMS) {
            FilmsScreen(onMovieClick = { navController.navigate(Routes.movieDetail(it)) })
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                onItemClick = { mediaType, id ->
                    when (mediaType) {
                        MediaType.TV -> navController.navigate(Routes.showPreview(id))
                        MediaType.MOVIE -> navController.navigate(Routes.moviePreview(id))
                    }
                },
            )
        }
        composable(Routes.STATS) { StatsScreen() }
        composable(Routes.SETTINGS) { SettingsScreen() }

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
            PersonDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
