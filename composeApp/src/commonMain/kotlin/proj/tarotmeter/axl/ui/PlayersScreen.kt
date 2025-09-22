package proj.tarotmeter.axl.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import proj.tarotmeter.axl.provider.PlayersProvider

/** Screen for managing players. Allows adding, renaming, and removing players. */
@Composable
fun PlayersScreen(playersProvider: PlayersProvider = koinInject()) {
  var newName by remember { mutableStateOf("") }
  Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      OutlinedTextField(
        value = newName,
        onValueChange = { newName = it },
        label = { Text("New player name") },
        modifier = Modifier.weight(1f),
      )
      Button(
        onClick = {
          val name = newName.trim()
          if (name.isNotEmpty()) {
            playersProvider.addPlayer(name)
            newName = ""
          }
        }
      ) {
        Text("Add")
      }
    }
    Divider()
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
      items(playersProvider.players, key = { it.id }) { p ->
        PlayerRow(
          p.name,
          onRename = { playersProvider.renamePlayer(p.id, it) },
          onDelete = { playersProvider.removePlayer(p.id) },
        )
      }
    }
  }
}

/**
 * A row component for displaying and managing a single player. Allows renaming and deleting a
 * player.
 *
 * @param name The player's name
 * @param onRename Callback for when the player is renamed
 * @param onDelete Callback for when the player is deleted
 */
@Composable
private fun PlayerRow(name: String, onRename: (String) -> Unit, onDelete: () -> Unit) {
  var editing by remember { mutableStateOf(false) }
  var value by remember { mutableStateOf(name) }
  val bg by
    animateColorAsState(
      if (editing) MaterialTheme.colorScheme.secondaryContainer
      else MaterialTheme.colorScheme.surface
    )
  Surface(
    shape = RoundedCornerShape(12.dp),
    tonalElevation = 2.dp,
    color = bg,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(
        Modifier.size(36.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer)
      )
      Spacer(Modifier.width(12.dp))
      if (editing) {
        OutlinedTextField(
          value = value,
          onValueChange = { value = it },
          modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        TextButton(
          onClick = {
            editing = false
            onRename(value)
          }
        ) {
          Text("Save")
        }
      } else {
        Text(value, modifier = Modifier.weight(1f))
        TextButton(onClick = { editing = true }) { Text("Rename") }
      }
      Spacer(Modifier.width(8.dp))
      TextButton(
        onClick = onDelete,
        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
      ) {
        Text("Delete")
      }
    }
  }
}
