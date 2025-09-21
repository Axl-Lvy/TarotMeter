package proj.tarotmeter.axl.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import proj.tarotmeter.axl.AppState
import proj.tarotmeter.axl.navigation.AppNavHost
import proj.tarotmeter.axl.navigation.Route

/**
 * The main application scaffold that provides the top-level UI structure.
 * This component handles the app bar, navigation, and content area layout.
 *
 * @param app The application state shared across screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(app: AppState) {
  val navController = rememberNavController()
  val backStackEntry by navController.currentBackStackEntryAsState()
  val route = backStackEntry?.destination?.route ?: Route.Home.route

  val title =
    when {
      route == Route.Home.route -> Route.Home.title
      route == Route.Players.route -> Route.Players.title
      route == Route.Settings.route -> Route.Settings.title
      route == Route.NewGame.route -> Route.NewGame.title
      route == Route.History.route -> Route.History.title
      route.startsWith(Route.Game.ROUTE) -> Route.Game.TITLE
      else -> "Tarot Meter"
    }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(title) },
        navigationIcon = {
          if (route != Route.Home.route) {
            TextButton(onClick = { navController.navigate(Route.Home.route) }) { Text("Home") }
          }
        },
      )
    }
  ) { padding ->
    Box(Modifier.fillMaxSize().padding(padding)) {
      AppNavHost(app = app, navController = navController)
    }
  }
}
