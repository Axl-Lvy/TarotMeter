package proj.tarotmeter.axl.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import proj.tarotmeter.axl.data.model.Game
import proj.tarotmeter.axl.data.model.Round
import proj.tarotmeter.axl.data.model.Scores
import proj.tarotmeter.axl.data.model.enums.Chelem
import proj.tarotmeter.axl.data.model.enums.Contract
import proj.tarotmeter.axl.data.model.enums.PetitAuBout
import proj.tarotmeter.axl.data.model.enums.Poignee
import proj.tarotmeter.axl.provider.GamesProvider

/**
 * Screen for editing a specific game. Displays game scores, allows adding rounds, and shows round
 * history.
 *
 * @param gameId The ID of the game to edit
 */
@Composable
fun GameEditorScreen(gameId: Int, gamesProvider: GamesProvider = koinInject()) {
  var game by remember { mutableStateOf<Game?>(null) }
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(gameId) { game = gamesProvider.getGame(gameId) }

  val currentGame = game
  if (currentGame == null) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Game not found") }
    return
  }
  Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    // Scores header
    Surface(
      shape = RoundedCornerShape(12.dp),
      tonalElevation = 2.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        currentGame.players.forEach { p ->
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
          ) {
            Text(p.name, fontWeight = FontWeight.Bold)
            val total = Scores.globalScores(currentGame).scores[p] ?: 0
            Text(
              "$total",
              style = MaterialTheme.typography.titleMedium,
              color = if (total >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
            )
          }
        }
      }
    }
    RoundEditor(
      game = currentGame,
      onAdd = { round ->
        coroutineScope.launch {
          gamesProvider.addRound(currentGame.id, round)
          game = gamesProvider.getGame(gameId)
        }
      },
    )
    Divider()
    Text("Rounds", style = MaterialTheme.typography.titleMedium)
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
      items(currentGame.rounds) { r ->
        Surface(
          shape = RoundedCornerShape(12.dp),
          tonalElevation = 1.dp,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(Modifier.padding(12.dp)) {
            val taker = currentGame.players.firstOrNull { it == r.taker }?.name ?: "?"
            val partner =
              r.partner?.let { id -> currentGame.players.firstOrNull { it == id }?.name }
            Text("${r.contract.title} – Taker: $taker" + (partner?.let { ", Partner: $it" } ?: ""))
            Text("Oudlers: ${r.oudlerCount} – Points: ${r.takerPoints}")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              val roundScore = Scores.roundScores(r, currentGame)
              currentGame.players.forEach { p ->
                val v = roundScore.forPlayer(p)
                Text("${p.name.split(' ').first()}: ${if (v >= 0) "+$v" else "$v"}")
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Component for adding a new round to a game. Allows selecting taker, partner, contract, oudler
 * count, and points.
 *
 * @param game The game to add a round to
 * @param onAdd Callback for when a new round is added
 */
@Composable
fun RoundEditor(game: Game, onAdd: (Round) -> Unit) {
  var takerIndex by remember { mutableStateOf(0) }
  var partnerIndex by remember { mutableStateOf(if (game.players.size == 5) 1 else -1) }
  var contract by remember { mutableStateOf(Contract.Garde) }
  var oudler by remember { mutableStateOf(1) }
  var pointsText by remember { mutableStateOf("41") }

  Surface(
    shape = RoundedCornerShape(12.dp),
    tonalElevation = 2.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text("Add Round", style = MaterialTheme.typography.titleMedium)
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Text("Taker", modifier = Modifier.align(Alignment.CenterVertically))
        Dropdown(game.players.map { it.name }, takerIndex, onChange = { takerIndex = it })
        if (game.players.size == 5) {
          Text("Partner", modifier = Modifier.align(Alignment.CenterVertically))
          Dropdown(game.players.map { it.name }, partnerIndex, onChange = { partnerIndex = it })
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Contract", modifier = Modifier.align(Alignment.CenterVertically))
        Dropdown(
          Contract.entries.map { it.title },
          Contract.entries.indexOf(contract),
          onChange = { contract = Contract.entries[it] },
        )
        Text("Oudlers", modifier = Modifier.align(Alignment.CenterVertically))
        Dropdown((0..3).map { it.toString() }, oudler, onChange = { oudler = it })
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = pointsText,
          onValueChange = { pointsText = it.filter { ch -> ch.isDigit() }.take(2) },
          label = { Text("Card points (0-91)") },
        )
        Button(
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
          }
        ) {
          Text("Add Round")
        }
      }
    }
  }
}

/**
 * A dropdown component for selecting from a list of options.
 *
 * @param options The list of options to display
 * @param selectedIndex The index of the currently selected option
 * @param onChange Callback for when a different option is selected
 */
@Composable
private fun Dropdown(options: List<String>, selectedIndex: Int, onChange: (Int) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  val label = options.getOrNull(selectedIndex).orEmpty()
  OutlinedButton(onClick = { expanded = true }) { Text(if (label.isEmpty()) "Select" else label) }
  DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
    options.forEachIndexed { idx, item ->
      DropdownMenuItem(
        text = { Text(item) },
        onClick = {
          onChange(idx)
          expanded = false
        },
      )
    }
  }
}
