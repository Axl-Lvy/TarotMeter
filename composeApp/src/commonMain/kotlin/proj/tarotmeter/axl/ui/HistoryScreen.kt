package proj.tarotmeter.axl.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import proj.tarotmeter.axl.core.data.model.Scores
import proj.tarotmeter.axl.core.provider.GamesProvider
import proj.tarotmeter.axl.ui.components.*
import tarotmeter.composeapp.generated.resources.*
import tarotmeter.composeapp.generated.resources.Res

/**
 * Screen for viewing game history. Displays a list of past games that can be selected for
 * viewing/editing.
 *
 * @param onOpenGame Callback for opening a specific game, with the game ID
 */
@Composable
fun HistoryScreen(onOpenGame: (Uuid) -> Unit, gamesProvider: GamesProvider = koinInject()) {
  var games by remember { mutableStateOf(emptyList<proj.tarotmeter.axl.core.data.model.Game>()) }
  LaunchedEffect(Unit) { games = gamesProvider.getGames() }

  ResponsiveContainer {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      if (games.isEmpty()) {
        EmptyState(
          message = stringResource(Res.string.history_empty_state),
          modifier = Modifier.weight(1f),
        )
      } else {
        Text(
          stringResource(
            if (games.size == 1) Res.string.history_game_count_single
            else Res.string.history_game_count_plural,
            games.size,
          ),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.weight(1f),
        ) {
          items(games, key = { it.id }) { game ->
            GameHistoryCard(game = game, onClick = { onOpenGame(game.id) })
          }
        }
      }
    }
  }
}

@Composable
private fun GameHistoryCard(game: proj.tarotmeter.axl.core.data.model.Game, onClick: () -> Unit) {
  proj.tarotmeter.axl.ui.components.CustomElevatedCard(onClick = onClick) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = stringResource(Res.string.history_game_label),
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.primary,
        )
        Surface(
          shape = MaterialTheme.shapes.small,
          color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
          Text(
            text =
              stringResource(
                if (game.rounds.size == 1) Res.string.history_round_count_single
                else Res.string.history_round_count_plural,
                game.rounds.size,
              ),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          )
        }
      }

      HorizontalDivider()

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        game.players.forEach { player -> PlayerAvatar(name = player.name) }
      }

      if (game.rounds.isNotEmpty()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
          val globalScores = Scores.globalScores(game)
          game.players.forEach { player ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = player.name.split(' ').first(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              val score = globalScores.scores[player] ?: 0
              ScoreText(score = score)
            }
          }
        }
      }
    }
  }
}
