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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.koin.compose.koinInject
import proj.tarotmeter.axl.data.DatabaseManager
import proj.tarotmeter.axl.data.model.Game
import proj.tarotmeter.axl.data.model.Player
import proj.tarotmeter.axl.data.model.Round
import proj.tarotmeter.axl.ui.navigation.AppNavHost
import proj.tarotmeter.axl.ui.navigation.Route
import proj.tarotmeter.axl.util.IdGenerator

/**
 * The main application scaffold that provides the top-level UI structure. This component handles
 * the app bar, navigation, and content area layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(db: DatabaseManager = koinInject()) {
  var isInitialized by remember { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    if (!isInitialized) {
      initializeIdGenerator(db)
      isInitialized = true
    }
  }
  if (!isInitialized) {
    // Show a loading state or splash screen while initializing
    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
      Text("Loading...")
    }
    return
  }
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
    Box(Modifier.fillMaxSize().padding(padding)) { AppNavHost(navController = navController) }
  }
}

private suspend fun initializeIdGenerator(db: DatabaseManager) {
  IdGenerator.initialize(Player::class, db.getMaxPlayerId())
  IdGenerator.initialize(Game::class, db.getMaxGameId())
  IdGenerator.initialize(Round::class, db.getMaxRoundId())
}
