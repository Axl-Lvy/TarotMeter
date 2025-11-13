package proj.tarotmeter.axl.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.resources.stringResource
import proj.tarotmeter.axl.ui.components.BackButton
import proj.tarotmeter.axl.ui.navigation.AppNavHost
import proj.tarotmeter.axl.ui.navigation.Route
import tarotmeter.composeapp.generated.resources.*
import tarotmeter.composeapp.generated.resources.Res

/**
 * The main application scaffold that provides the top-level UI structure. This component handles
 * the app bar, navigation, and content area layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(initialRoute: String? = null, onNavHostReady: suspend (NavController) -> Unit) {
  val navController = rememberNavController()
  val backStackEntry by navController.currentBackStackEntryAsState()
  val route = backStackEntry?.destination?.route ?: Route.Home.route

  val title =
    when {
      route == Route.Home.route -> stringResource(Res.string.title_home)
      route == Route.Players.route -> stringResource(Res.string.title_players)
      route == Route.Settings.route -> stringResource(Res.string.title_settings)
      route == Route.NewGame.route -> stringResource(Res.string.title_new_game)
      route == Route.History.route -> stringResource(Res.string.title_game_history)
      route.startsWith(Route.Game.ROUTE) -> stringResource(Res.string.title_game_editor)
      route.startsWith(Route.ConfirmEmail.ROUTE) -> stringResource(Res.string.title_confirm_email)
      route.startsWith(Route.JoinGame.ROUTE) -> stringResource(Res.string.title_join_game)
      else -> stringResource(Res.string.title_home)
    }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(title) },
        navigationIcon = {
          BackButton(route) {
            // If we can't pop back (e.g., direct navigation to a route), go to Home instead
            if (!navController.popBackStack()) {
              navController.navigate(Route.Home.route) {
                // Clear the back stack to prevent loops
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                // Avoid multiple copies of the same destination
                launchSingleTop = true
              }
            }
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          ),
      )
    }
  ) { padding ->
    Box(Modifier.fillMaxSize().padding(padding)) {
      AppNavHost(navController = navController, startDestination = initialRoute ?: Route.Home.route)
    }
  }
  LaunchedEffect(navController) { onNavHostReady(navController) }
}
