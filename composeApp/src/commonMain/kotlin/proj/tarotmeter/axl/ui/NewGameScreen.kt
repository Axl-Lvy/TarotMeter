package proj.tarotmeter.axl.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.uuid.Uuid
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.core.provider.GamesProvider
import proj.tarotmeter.axl.core.provider.PlayersProvider
import proj.tarotmeter.axl.ui.components.*

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
  var playerCount by remember { mutableStateOf(5) }
  var players by remember { mutableStateOf(emptyList<Player>()) }
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(Unit) { players = playersProvider.getPlayers() }

  ResponsiveContainer {
    Column(
      Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      SectionHeader("Create New Game")

      CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Text("Select Number of Players", style = MaterialTheme.typography.titleMedium)

          SegmentedButtons(
            options = listOf("3 Players", "4 Players", "5 Players"),
            selectedIndex = playerCount - 3,
            onSelect = { playerCount = it + 3 },
          )

          HorizontalDivider()

          Text("Available Players", style = MaterialTheme.typography.titleSmall)
          Text(
            "The first $playerCount players from your list will be used.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          if (players.isEmpty()) {
            Text(
              "No players available. Please add players first.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.error,
            )
          } else if (players.size < playerCount) {
            Text(
              "You need at least $playerCount players. Currently: ${players.size}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.error,
            )
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              players.take(playerCount).forEach { player ->
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                  PlayerAvatar(name = player.name)
                  Text(player.name, style = MaterialTheme.typography.bodyLarge)
                }
              }
            }
          }
        }
      }

      Spacer(Modifier.weight(1f))

      PrimaryButton(
        text = "Start Game",
        onClick = {
          coroutineScope.launch {
            val game = gamesProvider.createGame(playerCount)
            if (game != null) onGameCreated(game.id)
          }
        },
        enabled = players.size >= playerCount,
        modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp),
      )
    }
  }
}
