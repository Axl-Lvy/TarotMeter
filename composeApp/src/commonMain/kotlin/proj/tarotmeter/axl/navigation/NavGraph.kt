package proj.tarotmeter.axl.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import proj.tarotmeter.axl.AppState
import proj.tarotmeter.axl.ui.GameEditorScreen
import proj.tarotmeter.axl.ui.HistoryScreen
import proj.tarotmeter.axl.ui.HomeScreen
import proj.tarotmeter.axl.ui.NewGameScreen
import proj.tarotmeter.axl.ui.PlayersScreen
import proj.tarotmeter.axl.ui.SettingsScreen

/**
 * Main navigation part for the application. Defines all navigation routes and their corresponding
 * composable screens.
 *
 * @param app The application state shared across screens
 * @param navController Controller for navigation between screens
 * @param startDestination The initial route to display, defaults to Home
 */
@Composable
fun AppNavHost(
  app: AppState,
  navController: NavHostController,
  startDestination: String = Route.Home.route,
) {
  NavHost(navController = navController, startDestination = startDestination) {
    composable(Route.Home.route) {
      HomeScreen(
        onNewGame = { navController.navigate(Route.NewGame.route) },
        onPlayers = { navController.navigate(Route.Players.route) },
        onHistory = { navController.navigate(Route.History.route) },
        onSettings = { navController.navigate(Route.Settings.route) },
      )
    }
    composable(Route.Players.route) { PlayersScreen(app) }
    composable(Route.Settings.route) { SettingsScreen(app) }
    composable(Route.NewGame.route) {
      NewGameScreen(app = app, onGameCreated = { id -> navController.navigate(Route.Game(id)) })
    }
    composable(Route.History.route) {
      HistoryScreen(app = app, onOpenGame = { id -> navController.navigate(Route.Game(id)) })
    }
    composable<Route.Game> { backStackEntry ->
      val id = backStackEntry.toRoute<Route.Game>().id
      GameEditorScreen(app = app, gameId = id)
    }
  }
}
