package proj.tarotmeter.axl.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlin.uuid.Uuid
import proj.tarotmeter.axl.ui.pages.ConfirmEmailScreen
import proj.tarotmeter.axl.ui.pages.GameEditorScreen
import proj.tarotmeter.axl.ui.pages.HistoryScreen
import proj.tarotmeter.axl.ui.pages.HomeScreen
import proj.tarotmeter.axl.ui.pages.JoinGameScreen
import proj.tarotmeter.axl.ui.pages.NewGameScreen
import proj.tarotmeter.axl.ui.pages.PlayersScreen
import proj.tarotmeter.axl.ui.pages.SettingsScreen

/**
 * Main navigation part for the application. Defines all navigation routes and their corresponding
 * composable screens.
 *
 * @param navController Controller for navigation between screens
 * @param startDestination The initial route to display, defaults to Home
 */
@Composable
fun AppNavHost(navController: NavHostController, startDestination: String = Route.Home.route) {
  NavHost(navController = navController, startDestination = startDestination) {
    composable(Route.Home.route) {
      HomeScreen(
        onNewGame = { navController.navigate(Route.NewGame.route) },
        onPlayers = { navController.navigate(Route.Players.route) },
        onHistory = { navController.navigate(Route.History.route) },
        onSettings = { navController.navigate(Route.Settings.route) },
      )
    }
    composable(Route.Players.route) { PlayersScreen() }
    composable(Route.Settings.route) { SettingsScreen() }
    composable(Route.NewGame.route) {
      NewGameScreen(
        onGameCreated = { id ->
          navController.popBackStack()
          navController.navigate(Route.Game(id.toHexString()))
        }
      )
    }
    composable(Route.History.route) {
      HistoryScreen(onOpenGame = { id -> navController.navigate(Route.Game(id.toHexString())) })
    }
    composable<Route.Game> { backStackEntry ->
      val id = backStackEntry.toRoute<Route.Game>().id
      GameEditorScreen(gameId = Uuid.parseHex(id))
    }
    composable<Route.ConfirmEmail> { backStackEntry ->
      val tokenHash = backStackEntry.toRoute<Route.ConfirmEmail>().tokenHash
      ConfirmEmailScreen(tokenHash = tokenHash)
    }
    composable<Route.JoinGame> { backStackEntry ->
      val invitationCode = backStackEntry.toRoute<Route.JoinGame>().invitationCode
      JoinGameScreen(
        invitationCode = invitationCode,
        onSuccess = {
          navController.navigate(Route.History.route) {
            popUpTo(navController.graph.startDestinationId) { inclusive = false }
            launchSingleTop = true
          }
        },
      )
    }
  }
}
