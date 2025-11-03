package proj.tarotmeter.axl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.uuid.Uuid
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Round
import proj.tarotmeter.axl.core.data.model.Scores
import proj.tarotmeter.axl.core.provider.GamesProvider
import proj.tarotmeter.axl.ui.components.CustomElevatedCard
import proj.tarotmeter.axl.ui.components.EmptyState
import proj.tarotmeter.axl.ui.components.PlayerAvatar
import proj.tarotmeter.axl.ui.components.PlayerScoresRow
import proj.tarotmeter.axl.ui.components.ScoreText
import tarotmeter.composeapp.generated.resources.*
import tarotmeter.composeapp.generated.resources.Res

/**
 * Screen for editing a specific game. Displays game scores, allows adding rounds, and shows round
 * history.
 *
 * @param gameId The ID of the game to edit
 */
@Composable
fun GameEditorScreen(gameId: Uuid, gamesProvider: GamesProvider = koinInject()) {
  var game by remember { mutableStateOf<Game?>(null) }
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(gameId) { game = gamesProvider.getGame(gameId) }

  val currentGame = game
  if (currentGame == null) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    return
  }

  val globalScores = Scores.globalScores(currentGame)

  Column(Modifier.fillMaxSize()) {
    Spacer(modifier = Modifier.size(16.dp))
    // Fixed scores at the top
    PlayerScoresRow(
      playerScores = currentGame.players.map { it.name to (globalScores.scores[it] ?: 0) }
    )

    // Scrollable list with header content
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Spacer(modifier = Modifier.size(16.dp))
        // Add round section
        RoundEditor(
          game = currentGame,
          onAdd = { round ->
            coroutineScope.launch {
              gamesProvider.addRound(currentGame.id, round)
              game = gamesProvider.getGame(gameId)
            }
          },
        )
      }

      item { HorizontalDivider() }

      item {
        Text(
          stringResource(Res.string.game_editor_round_history, currentGame.rounds.size),
          style = MaterialTheme.typography.titleMedium,
        )
      }

      if (currentGame.rounds.isEmpty()) {
        item {
          EmptyState(
            message = stringResource(Res.string.game_editor_empty_state),
            modifier = Modifier.fillParentMaxHeight(0.3f),
          )
        }
      } else {
        items(currentGame.rounds.reversed()) { round ->
          RoundCard(round = round, game = currentGame)
        }
      }
    }
  }
}

@Composable
private fun RoundCard(round: Round, game: Game) {
  CustomElevatedCard {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      // Round header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = round.contract.title,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.primary,
        )
        Surface(
          shape = MaterialTheme.shapes.small,
          color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
          Text(
            text =
              pluralStringResource(
                Res.plurals.game_editor_oudler,
                round.oudlerCount,
                round.oudlerCount,
              ),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }

      // Taker and partner info
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        PlayerAvatar(name = round.taker.name, size = 32.dp)
        Column {
          Text(text = round.taker.name, style = MaterialTheme.typography.bodyMedium)
          Text(
            text = stringResource(Res.string.game_editor_taker_points, round.takerPoints),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        if (round.partner != null) {
          Text(
            stringResource(Res.string.game_editor_with),
            style = MaterialTheme.typography.bodySmall,
          )
          PlayerAvatar(name = round.partner.name, size = 32.dp)
          Text(text = round.partner.name, style = MaterialTheme.typography.bodyMedium)
        }
      }

      HorizontalDivider()

      // Round scores
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        val roundScore = Scores.roundScores(round, game)
        game.players.forEach { player ->
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = player.name.split(' ').first(),
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val score = roundScore.forPlayer(player)
            ScoreText(score = score)
          }
        }
      }
    }
  }
}
