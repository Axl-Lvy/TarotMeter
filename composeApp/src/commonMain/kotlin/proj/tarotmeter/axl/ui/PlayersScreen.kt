package proj.tarotmeter.axl.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.UserPlus
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import proj.tarotmeter.axl.core.provider.PlayersProvider
import proj.tarotmeter.axl.ui.components.*

/** Screen for managing players. Allows adding, renaming, and removing players. */
@Composable
fun PlayersScreen(playersProvider: PlayersProvider = koinInject()) {
  var newName by remember { mutableStateOf("") }
  var players by remember {
    mutableStateOf(emptyList<proj.tarotmeter.axl.core.data.model.Player>())
  }
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(Unit) { players = playersProvider.getPlayers() }

  ResponsiveContainer {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      SectionHeader("Manage Players")

      // Add player section
      proj.tarotmeter.axl.ui.components.ElevatedCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Add New Player", style = MaterialTheme.typography.titleMedium)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = newName,
              onValueChange = { newName = it },
              label = { Text("Player name") },
              modifier = Modifier.weight(1f),
              singleLine = true,
            )
            IconButton(
              onClick = {
                val name = newName.trim()
                if (name.isNotEmpty()) {
                  coroutineScope.launch {
                    playersProvider.addPlayer(name)
                    players = playersProvider.getPlayers()
                    newName = ""
                  }
                }
              },
              modifier = Modifier.size(56.dp),
            ) {
              Icon(FontAwesomeIcons.Solid.UserPlus, contentDescription = "Add player")
            }
          }
        }
      }

      if (players.isEmpty()) {
        EmptyState(
          message = "No players yet. Add your first player above!",
          modifier = Modifier.weight(1f),
        )
      } else {
        Text(
          "${players.size} player${if (players.size != 1) "s" else ""}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f),
        ) {
          items(players, key = { it.id }) { player ->
            EditablePlayerCard(
              name = player.name,
              onRename = { newName ->
                coroutineScope.launch {
                  playersProvider.renamePlayer(player.id, newName)
                  players = playersProvider.getPlayers()
                }
              },
              onDelete = {
                coroutineScope.launch {
                  playersProvider.removePlayer(player.id)
                  players = playersProvider.getPlayers()
                }
              },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun EditablePlayerCard(name: String, onRename: (String) -> Unit, onDelete: () -> Unit) {
  var editing by remember { mutableStateOf(false) }
  var editedName by remember(name) { mutableStateOf(name) }

  proj.tarotmeter.axl.ui.components.ElevatedCard {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        PlayerAvatar(name = name, size = 48.dp)

        if (editing) {
          OutlinedTextField(
            value = editedName,
            onValueChange = { editedName = it },
            modifier = Modifier.weight(1f),
            singleLine = true,
          )
        } else {
          Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
          )
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (editing) {
          SecondaryButton(
            text = "Cancel",
            onClick = {
              editing = false
              editedName = name
            },
            modifier = Modifier.weight(1f),
          )
          PrimaryButton(
            text = "Save",
            onClick = {
              editing = false
              onRename(editedName.trim())
            },
            modifier = Modifier.weight(1f),
            enabled = editedName.trim().isNotEmpty(),
          )
        } else {
          SecondaryButton(
            text = "Rename",
            onClick = { editing = true },
            modifier = Modifier.weight(1f),
          )
          OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.weight(1f),
            colors =
              ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
          ) {
            Text("Delete")
          }
        }
      }
    }
  }
}
