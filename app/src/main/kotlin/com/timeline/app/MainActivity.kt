package com.timeline.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.timeline.app.crash.readAndClearCrashLog
import com.timeline.app.ui.announcement.AnnouncementBanner
import com.timeline.app.ui.auth.AuthGateViewModel
import com.timeline.app.ui.auth.LoginScreen
import com.timeline.app.ui.navigation.BottomNavItem
import com.timeline.app.ui.navigation.TimeLineNavGraph
import com.timeline.app.ui.theme.TimeLineTheme
import com.timeline.app.ui.update.UpdateBanner
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimeLineTheme {
                TimeLineApp()
            }
        }
    }
}

@Composable
private fun TimeLineApp() {
    val context = LocalContext.current
    var crashLog by remember { mutableStateOf(readAndClearCrashLog(context)) }
    crashLog?.let { log ->
        CrashLogDialog(log = log, onDismiss = { crashLog = null })
    }

    val authGateViewModel: AuthGateViewModel = hiltViewModel()
    val isSignedIn by authGateViewModel.isSignedIn.collectAsStateWithLifecycle()

    when (isSignedIn) {
        null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        false -> LoginScreen()
        true -> TimeLineAppContent()
    }
}

/** Shown once, the launch right after a crash — there's no adb/Android Studio access on the
 * device this runs on, so this is the only practical way to see what actually crashed. */
@Composable
private fun CrashLogDialog(log: String, onDismiss: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reelia a planté") },
        text = {
            SelectionContainer {
                Text(
                    log,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { clipboardManager.setText(AnnotatedString(log)) }) {
                Text("Copier")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        },
    )
}

@Composable
private fun TimeLineAppContent() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    Scaffold(
        topBar = {
            Column(Modifier.statusBarsPadding()) {
                UpdateBanner()
                AnnouncementBanner()
            }
        },
        bottomBar = {
            val showBottomBar = BottomNavItem.entries.any { item ->
                currentRoute?.hierarchy?.any { it.route == item.route } == true
            }
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                    BottomNavItem.entries.forEach { item ->
                        val selected = currentRoute?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) item.icon else item.outlineIcon,
                                    contentDescription = stringResource(item.labelRes),
                                )
                            },
                            label = { Text(stringResource(item.labelRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color.Transparent,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        TimeLineNavGraph(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}
