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
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Round
import proj.tarotmeter.axl.core.data.model.Scores
import proj.tarotmeter.axl.core.data.model.enums.Chelem
import proj.tarotmeter.axl.core.data.model.enums.Contract
import proj.tarotmeter.axl.core.data.model.enums.PetitAuBout
import proj.tarotmeter.axl.core.data.model.enums.Poignee
import proj.tarotmeter.axl.core.provider.GamesProvider
import proj.tarotmeter.axl.ui.components.*

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

  ResponsiveContainer {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      SectionHeader("Game in Progress")

      // Current scores
      val globalScores = Scores.globalScores(currentGame)
      PlayerScoresRow(
        playerScores = currentGame.players.map { it.name to (globalScores.scores[it] ?: 0) }
      )

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

      HorizontalDivider()

      // Rounds history
      Text(
        "Round History (${currentGame.rounds.size})",
        style = MaterialTheme.typography.titleMedium,
      )

      if (currentGame.rounds.isEmpty()) {
        EmptyState(
          message = "No rounds yet. Add your first round above!",
          modifier = Modifier.weight(1f),
        )
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.weight(1f),
        ) {
          items(currentGame.rounds.reversed()) { round ->
            RoundCard(round = round, game = currentGame)
          }
        }
      }
    }
  }
}

@Composable
private fun RoundEditor(game: Game, onAdd: (Round) -> Unit) {
  var takerIndex by remember { mutableStateOf(0) }
  var partnerIndex by remember { mutableStateOf(if (game.players.size == 5) 1 else -1) }
  var contract by remember { mutableStateOf(Contract.GARDE) }
  var oudler by remember { mutableStateOf(1) }
  var pointsText by remember { mutableStateOf("41") }

  proj.tarotmeter.axl.ui.components.ElevatedCard(modifier = Modifier.fillMaxWidth()) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Text("Add New Round", style = MaterialTheme.typography.titleMedium)

      ResponsiveTwoColumn(
        leftContent = {
          TarotDropdown(
            label = "Taker",
            options = game.players.map { it.name },
            selectedIndex = takerIndex,
            onSelect = { takerIndex = it },
          )

          if (game.players.size == 5) {
            TarotDropdown(
              label = "Partner",
              options = game.players.map { it.name },
              selectedIndex = partnerIndex,
              onSelect = { partnerIndex = it },
            )
          }
        },
        rightContent = {
          TarotDropdown(
            label = "Contract",
            options = Contract.entries.map { it.title },
            selectedIndex = Contract.entries.indexOf(contract),
            onSelect = { contract = Contract.entries[it] },
          )

          TarotDropdown(
            label = "Oudlers",
            options = (0..3).map { "$it Oudler${if (it != 1) "s" else ""}" },
            selectedIndex = oudler,
            onSelect = { oudler = it },
          )
        },
      )

      OutlinedTextField(
        value = pointsText,
        onValueChange = { pointsText = it.filter { ch -> ch.isDigit() }.take(2) },
        label = { Text("Card points (0-91)") },
        modifier = Modifier.fillMaxWidth(),
      )

      PrimaryButton(
        text = "Add Round",
        onClick = {
          val taker = game.players[takerIndex.coerceIn(0, game.players.lastIndex)]
          val partner =
            if (game.players.size == 5)
              game.players[partnerIndex.coerceIn(0, game.players.lastIndex)]
            else null
          val round =
            Round(
              taker = taker,
              partner = partner,
              contract = contract,
              oudlerCount = oudler,
              takerPoints = pointsText.toIntOrNull()?.coerceIn(0, 91) ?: 0,
              poignee = Poignee.NONE,
              petitAuBout = PetitAuBout.NONE,
              chelem = Chelem.NONE,
            )
          onAdd(round)
          // Reset form
          pointsText = "41"
        },
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun RoundCard(round: Round, game: Game) {
  proj.tarotmeter.axl.ui.components.ElevatedCard {
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
            text = "${round.oudlerCount} Oudler${if (round.oudlerCount != 1) "s" else ""}",
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
            text = "Taker • ${round.takerPoints} pts",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        if (round.partner != null) {
          Text("with", style = MaterialTheme.typography.bodySmall)
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
