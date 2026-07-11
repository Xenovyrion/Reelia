package com.timeline.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.timeline.app.domain.model.MediaType
import com.timeline.app.ui.addmedia.AddMediaScreen
import com.timeline.app.ui.library.LibraryScreen
import com.timeline.app.ui.moviedetail.MovieDetailScreen
import com.timeline.app.ui.settings.SettingsScreen
import com.timeline.app.ui.showdetail.ShowDetailScreen
import com.timeline.app.ui.stats.StatsScreen
import com.timeline.app.ui.upnext.UpNextScreen

@Composable
fun TimeLineNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Routes.LIBRARY, modifier = modifier) {
        composable(Routes.LIBRARY) {
            LibraryScreen(
                onShowClick = { navController.navigate(Routes.showDetail(it)) },
                onMovieClick = { navController.navigate(Routes.movieDetail(it)) },
                onAddClick = { navController.navigate(Routes.ADD_MEDIA) },
            )
        }
        composable(Routes.ADD_MEDIA) {
            AddMediaScreen(
                onBack = { navController.popBackStack() },
                onAdded = { mediaType, id ->
                    navController.popBackStack()
                    when (mediaType) {
                        MediaType.TV -> navController.navigate(Routes.showDetail(id))
                        MediaType.MOVIE -> navController.navigate(Routes.movieDetail(id))
                    }
                },
            )
        }
        composable(
            route = Routes.SHOW_DETAIL,
            arguments = listOf(navArgument("showId") { type = NavType.IntType }),
        ) {
            ShowDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.MOVIE_DETAIL,
            arguments = listOf(navArgument("movieId") { type = NavType.IntType }),
        ) {
            MovieDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.UP_NEXT) { UpNextScreen() }
        composable(Routes.STATS) { StatsScreen() }
        composable(Routes.SETTINGS) { SettingsScreen() }
    }
}
