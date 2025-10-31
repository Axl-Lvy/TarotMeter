package proj.tarotmeter.axl.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.ArrowAltCircleLeft
import proj.tarotmeter.axl.ui.navigation.AppNavHost
import proj.tarotmeter.axl.ui.navigation.Route
import proj.tarotmeter.axl.ui.navigation.rememberPlatformNavController

/**
 * The main application scaffold that provides the top-level UI structure. This component handles
 * the app bar, navigation, and content area layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
  val navController = rememberPlatformNavController()
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

  val showBackButton = route != Route.Home.route

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(title) },
        navigationIcon = {
          if (showBackButton) {
            IconButton(onClick = { navController.popBackStack() }) {
              Icon(
                imageVector = FontAwesomeIcons.Regular.ArrowAltCircleLeft,
                contentDescription = "Navigate back",
              )
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
    Box(Modifier.fillMaxSize().padding(padding)) { AppNavHost(navController = navController) }
  }
}
