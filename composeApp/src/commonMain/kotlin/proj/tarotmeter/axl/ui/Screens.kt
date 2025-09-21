package proj.tarotmeter.axl.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import proj.tarotmeter.axl.AppState
import proj.tarotmeter.axl.model.*

/**
 * The home screen of the application. Provides navigation buttons to other screens and a brief app
 * introduction.
 *
 * @param app The application state
 * @param onNewGame Callback for creating a new game
 * @param onPlayers Callback for navigating to the players screen
 * @param onHistory Callback for navigating to the game history screen
 * @param onSettings Callback for navigating to the settings screen
 */
@Composable
fun HomeScreen(
  app: AppState,
  onNewGame: () -> Unit,
  onPlayers: () -> Unit,
  onHistory: () -> Unit,
  onSettings: () -> Unit,
) {
  val gradient =
    Brush.verticalGradient(listOf(Color(0xFF121212), MaterialTheme.colorScheme.primaryContainer))
  Column(
    modifier = Modifier.fillMaxSize().background(gradient).padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
      Text(
        "Tarot Meter",
        style =
          MaterialTheme.typography.headlineMedium.copy(
            color = Color.White,
            fontWeight = FontWeight.Bold,
          ),
      )
      Spacer(Modifier.height(8.dp))
      Text("Track your Tarot games with style", color = Color(0xFFECECEC))
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      PrimaryAction("New Game") { onNewGame() }
      SecondaryAction("Players") { onPlayers() }
      SecondaryAction("Game History") { onHistory() }
      SecondaryAction("Settings") { onSettings() }
    }
    Text(
      "No data is persisted yet. Database will be added later.",
      color = Color(0xFFDDDDDD),
      textAlign = TextAlign.Center,
    )
  }
}

/**
 * A primary action button with full width and rounded corners.
 *
 * @param text The button text
 * @param onClick Callback for when the button is clicked
 */
@Composable
private fun PrimaryAction(text: String, onClick: () -> Unit) {
  Button(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth().height(56.dp),
    shape = RoundedCornerShape(16.dp),
  ) {
    Text(text)
  }
}

/**
 * A secondary action button with full width, rounded corners, and an outlined style.
 *
 * @param text The button text
 * @param onClick Callback for when the button is clicked
 */
@Composable
private fun SecondaryAction(text: String, onClick: () -> Unit) {
  OutlinedButton(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth().height(56.dp),
    shape = RoundedCornerShape(16.dp),
  ) {
    Text(text)
  }
}

/**
 * Screen for managing players. Allows adding, renaming, and removing players.
 *
 * @param app The application state
 */
@Composable
fun PlayersScreen(app: AppState) {
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
            app.addPlayer(name)
            newName = ""
          }
        }
      ) {
        Text("Add")
      }
    }
    Divider()
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
      items(app.players, key = { it.id }) { p ->
        PlayerRow(
          p.name,
          onRename = { app.renamePlayer(p.id, it) },
          onDelete = { app.removePlayer(p.id) },
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

/**
 * Screen for application settings. Allows toggling dark mode and hints.
 *
 * @param app The application state
 */
@Composable
fun SettingsScreen(@Suppress("UNUSED_PARAMETER") app: AppState) {
  var darkMode by remember { mutableStateOf(true) }
  var showTips by remember { mutableStateOf(true) }
  Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("Common Settings", style = MaterialTheme.typography.titleMedium)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      Text("Dark Mode", modifier = Modifier.weight(1f))
      Switch(checked = darkMode, onCheckedChange = { darkMode = it })
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      Text("Show Hints", modifier = Modifier.weight(1f))
      Switch(checked = showTips, onCheckedChange = { showTips = it })
    }
    Text("These are placeholders. Persist them later with your database.", color = Color.Gray)
  }
}

/**
 * Screen for creating a new game. Allows selecting the number of players and starting a new game.
 *
 * @param app The application state
 * @param onBack Callback for returning to the previous screen
 * @param onGameCreated Callback for when a new game is created, with the game ID
 */
@Composable
fun NewGameScreen(app: AppState, onBack: () -> Unit, onGameCreated: (Int) -> Unit) {
  var count by remember { mutableStateOf(5) }
  Column(
    Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text("Select number of players", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      (3..5).forEach { n ->
        val selected = count == n
        val colors =
          if (selected) ButtonDefaults.filledTonalButtonColors()
          else ButtonDefaults.outlinedButtonColors()
        if (selected) FilledTonalButton(onClick = { count = n }) { Text("$n") }
        else OutlinedButton(onClick = { count = n }) { Text("$n") }
      }
    }
    if (app.players.size < count) {
      Text(
        "Not enough players (${app.players.size}/$count). Add more in Players page.",
        color = MaterialTheme.colorScheme.error,
      )
    }
    Button(
      onClick = {
        val game = app.createGame(count)
        if (game != null) onGameCreated(game.id)
      },
      enabled = app.players.size >= count,
    ) {
      Text("Create Game")
    }
    Spacer(Modifier.height(8.dp))
    Text("Players used will be the first $count from your Players list.", color = Color.Gray)
  }
}

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

/**
 * Screen for editing a specific game. Displays game scores, allows adding rounds, and shows round
 * history.
 *
 * @param app The application state
 * @param gameId The ID of the game to edit
 */
@Composable
fun GameEditorScreen(app: AppState, gameId: Int) {
  val game = app.getGame(gameId)
  if (game == null) {
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
        game.players.forEach { p ->
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
          ) {
            Text(p.name, fontWeight = FontWeight.Bold)
            val total = app.totalScore(game, p)
            Text(
              "$total",
              style = MaterialTheme.typography.titleMedium,
              color = if (total >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
            )
          }
        }
      }
    }
    RoundEditor(game = game, onAdd = { app.addRound(game.id, it) })
    Divider()
    Text("Rounds", style = MaterialTheme.typography.titleMedium)
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
      items(game.rounds) { r ->
        Surface(
          shape = RoundedCornerShape(12.dp),
          tonalElevation = 1.dp,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(Modifier.padding(12.dp)) {
            val taker = game.players.firstOrNull { it == r.taker }?.name ?: "?"
            val partner = r.partner?.let { id -> game.players.firstOrNull { it == id }?.name }
            Text("${r.contract.title} – Taker: $taker" + (partner?.let { ", Partner: $it" } ?: ""))
            Text("Oudlers: ${r.oudlerCount} – Points: ${r.takerPoints}")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              val roundScore = Scores.roundScores(r, game)
              game.players.forEach { p ->
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
