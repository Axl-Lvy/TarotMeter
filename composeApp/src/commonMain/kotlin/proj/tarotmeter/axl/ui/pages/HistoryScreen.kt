package proj.tarotmeter.axl.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.uuid.Uuid
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Scores
import proj.tarotmeter.axl.core.provider.DataProvider
import proj.tarotmeter.axl.ui.components.CustomElevatedCard
import proj.tarotmeter.axl.ui.components.EmptyState
import proj.tarotmeter.axl.ui.components.GameSourceBadge
import proj.tarotmeter.axl.ui.components.JoinGameDialog
import proj.tarotmeter.axl.ui.components.PlayerAvatar
import proj.tarotmeter.axl.ui.components.ResponsiveContainer
import proj.tarotmeter.axl.ui.components.ScoreText
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.history_empty_state
import tarotmeter.composeapp.generated.resources.history_game_count
import tarotmeter.composeapp.generated.resources.history_join_game
import tarotmeter.composeapp.generated.resources.history_round_count

/**
 * Screen for viewing game history. Displays a list of past games that can be selected for
 * viewing/editing.
 *
 * @param onOpenGame Callback for opening a specific game, with the game ID
 */
@Composable
fun HistoryScreen(onOpenGame: (Uuid) -> Unit, dataProvider: DataProvider = koinInject()) {
  var games by remember { mutableStateOf(emptyList<Game>()) }
  var showJoinGameDialog by remember { mutableStateOf(false) }
  var isRefreshing by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) { games = dataProvider.getGames() }
  LaunchedEffect(isRefreshing) {
    if (isRefreshing) {
      games = dataProvider.getGames()
      isRefreshing = false
    }
  }

  PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = { isRefreshing = true }) {
    ResponsiveContainer {
      Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            pluralStringResource(Res.plurals.history_game_count, games.size, games.size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          FloatingActionButton(
            onClick = { showJoinGameDialog = true },
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 16.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(Res.string.history_join_game),
                modifier = Modifier.size(20.dp),
              )
              Text(
                text = stringResource(Res.string.history_join_game),
                style = MaterialTheme.typography.labelLarge,
              )
            }
          }
        }

        if (games.isEmpty()) {
          EmptyState(
            message = stringResource(Res.string.history_empty_state),
            modifier = Modifier.weight(1f),
          )
        } else {

          Spacer(modifier = Modifier.size(8.dp))

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

  if (showJoinGameDialog) {
    JoinGameDialog(
      onDismiss = { showJoinGameDialog = false },
      onSuccess = {
        showJoinGameDialog = false
        // Refresh games list
        games = emptyList()
      },
    )
  }
}

@Composable
private fun GameHistoryCard(game: Game, onClick: () -> Unit) {
  CustomElevatedCard(onClick = onClick) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = game.name,
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.primary,
        )
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          GameSourceBadge(source = game.source)
          Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.secondaryContainer,
          ) {
            Text(
              text =
                pluralStringResource(
                  Res.plurals.history_round_count,
                  game.rounds.size,
                  game.rounds.size,
                ),
              style = MaterialTheme.typography.labelMedium,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            )
          }
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
