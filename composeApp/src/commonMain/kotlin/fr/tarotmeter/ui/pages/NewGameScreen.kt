package fr.tarotmeter.ui.pages

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.OutlinedTextField
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
import fr.tarotmeter.core.data.model.Player
import fr.tarotmeter.core.provider.DataProvider
import fr.tarotmeter.ui.components.CustomElevatedCard
import fr.tarotmeter.ui.components.PlayerAvatar
import fr.tarotmeter.ui.components.PrimaryButton
import fr.tarotmeter.ui.components.SectionHeader
import kotlin.uuid.Uuid
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.new_game_button_start
import tarotmeter.composeapp.generated.resources.new_game_header
import tarotmeter.composeapp.generated.resources.new_game_name_label
import tarotmeter.composeapp.generated.resources.new_game_name_placeholder
import tarotmeter.composeapp.generated.resources.new_game_no_players
import tarotmeter.composeapp.generated.resources.new_game_select_players
import tarotmeter.composeapp.generated.resources.new_game_selected_count

/**
 * Screen for creating a new game. Allows selecting the number of players and starting a new game.
 *
 * @param onGameCreated Callback for when a new game is created, with the game ID
 */
@Composable
fun NewGameScreen(onGameCreated: (Uuid) -> Unit, dataProvider: DataProvider = koinInject()) {
  var availablePlayers by remember { mutableStateOf(emptyList<Player>()) }
  val selectedPlayers = remember { mutableStateSetOf<Player>() }
  var gameName by remember { mutableStateOf("") }
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(Unit) { availablePlayers = dataProvider.getPlayers() }

  val selectedCount = selectedPlayers.size
  val isValidSelection = selectedCount in 3..5 && gameName.trim().isNotBlank()

  Column(
    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    SectionHeader(stringResource(Res.string.new_game_header))

    CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
          stringResource(Res.string.new_game_select_players),
          style = MaterialTheme.typography.titleMedium,
        )

        OutlinedTextField(
          value = gameName,
          onValueChange = { gameName = it },
          label = { Text(stringResource(Res.string.new_game_name_label)) },
          placeholder = { Text(stringResource(Res.string.new_game_name_placeholder)) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
        )

        Text(
          pluralStringResource(Res.plurals.new_game_selected_count, selectedCount, selectedCount),
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
          text = stringResource(Res.string.new_game_button_start),
          onClick = {
            coroutineScope.launch {
              val game = dataProvider.createGame(selectedPlayers, gameName)
              onGameCreated(game.id)
            }
          },
          enabled = isValidSelection,
          modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp),
        )

        HorizontalDivider()

        if (availablePlayers.isEmpty()) {
          Text(
            stringResource(Res.string.new_game_no_players),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
          )
        } else {
          PlayersList(availablePlayers, selectedPlayers)
        }
      }
    }
  }
}

/**
 * Displays a list of selectable players.
 *
 * Each player can be selected or deselected by clicking on their entry.
 *
 * @param availablePlayers List of players to choose from
 * @param selectedPlayers Mutable set of currently selected players
 */
@Composable
private fun PlayersList(availablePlayers: List<Player>, selectedPlayers: MutableSet<Player>) {
  val scrollState = rememberScrollState()
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
