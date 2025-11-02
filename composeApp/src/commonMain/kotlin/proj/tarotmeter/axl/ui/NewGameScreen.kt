package proj.tarotmeter.axl.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.uuid.Uuid
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.core.provider.GamesProvider
import proj.tarotmeter.axl.core.provider.PlayersProvider
import proj.tarotmeter.axl.ui.components.CustomElevatedCard
import proj.tarotmeter.axl.ui.components.PlayerAvatar
import proj.tarotmeter.axl.ui.components.PrimaryButton
import proj.tarotmeter.axl.ui.components.SectionHeader

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
  var availablePlayers by remember { mutableStateOf(emptyList<Player>()) }
  val selectedPlayers = remember { mutableStateSetOf<Player>() }
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(Unit) { availablePlayers = playersProvider.getPlayers() }

  val selectedCount = selectedPlayers.size
  val isValidSelection = selectedCount in 3..5

  Column(
    Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    val scrollState = rememberScrollState()
    SectionHeader("Create New Game")

    CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Select Players", style = MaterialTheme.typography.titleMedium)

        Text(
          "Selected: $selectedCount player${if (selectedCount != 1) "s" else ""} (3-5 required)",
          style = MaterialTheme.typography.bodySmall,
          color =
            if (isValidSelection) {
              MaterialTheme.colorScheme.onSurfaceVariant
            } else {
              MaterialTheme.colorScheme.error
            },
        )

        Spacer(Modifier.weight(1f))

        PrimaryButton(
          text = "Start Game",
          onClick = {
            coroutineScope.launch {
              val game = gamesProvider.createGame(selectedPlayers)
              onGameCreated(game.id)
            }
          },
          enabled = isValidSelection,
          modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp),
        )

        HorizontalDivider()

        if (availablePlayers.isEmpty()) {
          Text(
            "No players available. Please add players first.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
          )
        } else {
          SelectablePlayers(scrollState, availablePlayers, selectedPlayers)
        }
      }
    }
  }
}

@Composable
private fun SelectablePlayers(
  scrollState: ScrollState,
  availablePlayers: List<Player>,
  selectedPlayers: MutableSet<Player>,
) {
  Column(
    modifier = Modifier.fillMaxWidth().fillMaxHeight().verticalScroll(scrollState),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    availablePlayers.forEach { player ->
      val isSelected = selectedPlayers.contains(player)
      Surface(
        onClick = {
          if (isSelected) {
            selectedPlayers.remove(player)
          } else {
            selectedPlayers.add(player)
          }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color =
          if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
          } else {
            MaterialTheme.colorScheme.surface
          },
        border =
          if (isSelected) {
            null
          } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
          },
      ) {
        SelectablePlayer(player, isSelected)
      }
    }
  }
}

@Composable
private fun SelectablePlayer(player: Player, isSelected: Boolean) {
  Row(
    modifier = Modifier.padding(12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    PlayerAvatar(name = player.name)
    Text(
      player.name,
      style = MaterialTheme.typography.bodyLarge,
      color =
        if (isSelected) {
          MaterialTheme.colorScheme.onPrimaryContainer
        } else {
          MaterialTheme.colorScheme.onSurface
        },
    )
    Spacer(Modifier.weight(1f))
    Checkbox(checked = isSelected, onCheckedChange = null)
  }
}
