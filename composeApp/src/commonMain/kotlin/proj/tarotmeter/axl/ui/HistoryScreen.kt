package proj.tarotmeter.axl.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import proj.tarotmeter.axl.AppState

/**
 * Screen for viewing game history. Displays a list of past games that can be selected for
 * viewing/editing.
 *
 * @param app The application state
 * @param onOpenGame Callback for opening a specific game, with the game ID
 */
@Composable
fun HistoryScreen(app: AppState, onOpenGame: (Int) -> Unit) {
  if (app.games.isEmpty()) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("No games yet. Start a New Game.")
    }
    return
  }
  LazyColumn(
    Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    items(app.games, key = { it.id }) { game ->
      Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().clickable { onOpenGame(game.id) },
      ) {
        Column(Modifier.padding(12.dp)) {
          Text("Game ${game.id}", style = MaterialTheme.typography.titleMedium)
          Text("Players: ${game.players.joinToString { it.name }}", color = Color.Gray)
          Text("Rounds: ${game.rounds.size}", color = Color.Gray)
        }
      }
    }
  }
}
