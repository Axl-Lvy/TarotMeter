package proj.tarotmeter.axl.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.uuid.Uuid
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import proj.tarotmeter.axl.provider.GamesProvider
import proj.tarotmeter.axl.provider.PlayersProvider

/**
 * Screen for creating a new game. Allows selecting the number of players and starting a new game.
 *
 * @param onGameCreated Callback for when a new game is created, with the game ID
 */
@Composable
fun NewGameScreen(
  onGameCreated: (Uuid) -> Unit,
  playersProvider: PlayersProvider = koinInject(),
  gamesProvider: GamesProvider = koinInject(),
) {
  var count by remember { mutableStateOf(5) }
  var players by remember { mutableStateOf(emptyList<proj.tarotmeter.axl.data.model.Player>()) }
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(Unit) { players = playersProvider.getPlayers() }
  Column(
    Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text("Select number of players", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      (3..5).forEach { n ->
        val selected = count == n
        val colors =
          if (selected) ButtonDefaults.filledTonalButtonColors()
          else ButtonDefaults.outlinedButtonColors()
        if (selected) FilledTonalButton(onClick = { count = n }) { Text("$n") }
        else OutlinedButton(onClick = { count = n }) { Text("$n") }
      }
    }
    if (players.size < count) {
      Text(
        "Not enough players (${players.size}/$count). Add more in Players page.",
        color = MaterialTheme.colorScheme.error,
      )
    }
    Button(
      onClick = {
        coroutineScope.launch {
          val game = gamesProvider.createGame(count)
          if (game != null) onGameCreated(game.id)
        }
      },
      enabled = players.size >= count,
    ) {
      Text("Create Game")
    }
    Spacer(Modifier.height(8.dp))
    Text("Players used will be the first $count from your Players list.", color = Color.Gray)
  }
}
