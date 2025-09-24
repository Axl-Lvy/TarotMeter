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
import proj.tarotmeter.axl.Screen
import proj.tarotmeter.axl.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(app: AppState) {
  val topTitle =
    when (app.currentScreen) {
      Screen.Home -> "Tarot Meter"
      Screen.Players -> "Players"
      Screen.Settings -> "Settings"
      Screen.NewGame -> "New Game"
      Screen.History -> "Game History"
      is Screen.GameEditor -> "Game Editor"
    }
  
  val lightBackground = Color(0xFFF9FAFB)
  val subtleBlue = Color(0xFFF1F5F9)
  val accentPurple = Color(0xFFF8FAFC)
  
  // Modern gradient background for scaffold
  val scaffoldBackground = Brush.radialGradient(
    colors = listOf(
      lightBackground,
      subtleBlue,
      accentPurple
    ),
    radius = 1200f,
    center = androidx.compose.ui.geometry.Offset(0.5f, 0.3f)
  )
  
  val primaryAccent = Color(0xFF06B6D4)
  val darkText = Color(0xFF111827)
  
  Scaffold(
    containerColor = Color.Transparent,
    topBar = {
      TopAppBar(
        title = { 
          Text(
            topTitle,
            color = darkText,
            fontWeight = FontWeight.SemiBold
          ) 
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Color.White
        ),
        navigationIcon = {
          if (app.currentScreen !is Screen.Home) {
            TextButton(
              onClick = { app.navigate(Screen.Home) },
              colors = ButtonDefaults.textButtonColors(contentColor = primaryAccent)
            ) { 
              Text("🏠 Home") 
            }
          }
        },
      )
    }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(scaffoldBackground)
        .padding(padding)
    ) {
      when (val s = app.currentScreen) {
        Screen.Home -> HomeScreen(app)
        Screen.Players -> PlayersScreen(app)
        Screen.Settings -> SettingsScreen(app)
        Screen.NewGame -> NewGameScreen(app)
        Screen.History -> HistoryScreen(app)
        is Screen.GameEditor -> GameEditorScreen(app, s.gameId)
      }
    }
  }
}

@Composable
private fun HomeScreen(app: AppState) {
  val lightBackground = Color(0xFFF9FAFB)
  val subtleBlue = Color(0xFFF1F5F9)
  val accentPurple = Color(0xFFF8FAFC)
  
  // Modern gradient background with subtle depth
  val modernBackground = Brush.radialGradient(
    colors = listOf(
      lightBackground,
      subtleBlue,
      accentPurple
    ),
    radius = 1200f,
    center = androidx.compose.ui.geometry.Offset(0.5f, 0.3f)
  )
  
  val primaryGradient = listOf(Color(0xFF06B6D4), Color(0xFF10B981))
  val secondaryAccent = Color(0xFF6366F1)
  val darkText = Color(0xFF111827)
  val mediumGray = Color(0xFF6B7280)
  val lightBorder = Color(0xFFE5E7EB)
  
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(modernBackground)
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    // Header Section
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        "Tarot Meter",
        style = MaterialTheme.typography.displayLarge.copy(
          color = darkText,
          fontWeight = FontWeight.Bold
        ),
        textAlign = TextAlign.Center
      )
      Spacer(Modifier.height(16.dp))
      Text(
        "Track your tarot games with style",
        style = MaterialTheme.typography.titleLarge.copy(
          color = mediumGray,
          fontWeight = FontWeight.Medium
        ),
        textAlign = TextAlign.Center
      )
    }
    
    Spacer(Modifier.height(80.dp))
    
    // Main Action - New Game (Gradient)
    PrimaryGradientButton(
      text = "🎮 New Game",
      onClick = { app.navigate(Screen.NewGame) },
      gradientColors = primaryGradient,
      modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(Modifier.height(48.dp))
    
    // Secondary Actions (Clean Cards)
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      ModernCardButton(
        text = "👥 Players",
        onClick = { app.navigate(Screen.Players) },
        accentColor = secondaryAccent
      )
      
      ModernCardButton(
        text = "📊 History",
        onClick = { app.navigate(Screen.History) },
        accentColor = Color(0xFF8B5CF6)
      )
      
      ModernCardButton(
        text = "⚙️ Settings",
        onClick = { app.navigate(Screen.Settings) },
        accentColor = mediumGray
      )
    }
  }
}

@Composable
private fun PrimaryGradientButton(
  text: String,
  onClick: () -> Unit,
  gradientColors: List<Color>,
  modifier: Modifier = Modifier
) {
  val gradient = Brush.linearGradient(gradientColors)
  
  Box(
    modifier = modifier
      .height(68.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(gradient)
      .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    )
  }
}

@Composable
private fun ModernCardButton(
  text: String,
  onClick: () -> Unit,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  var isPressed by remember { mutableStateOf(false) }
  
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = Color.White,
    modifier = modifier
      .fillMaxWidth()
      .height(72.dp)
      .clickable { onClick() },
    shadowElevation = if (isPressed) 8.dp else 4.dp,
    border = androidx.compose.foundation.BorderStroke(
      width = 1.dp, 
      color = Color(0xFFE5E7EB)
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Icon with accent color background
      Surface(
        shape = CircleShape,
        color = accentColor.copy(alpha = 0.1f),
        modifier = Modifier.size(48.dp)
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = text.split(" ").firstOrNull() ?: "🎯",
            style = MaterialTheme.typography.titleLarge
          )
        }
      }
      
      Spacer(Modifier.width(20.dp))
      
      Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
          color = Color(0xFF111827),
          fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.weight(1f)
      )
      
      // Subtle arrow
      Text(
        text = "→",
        style = MaterialTheme.typography.titleMedium.copy(
          color = Color(0xFF6B7280)
        )
      )
    }
  }
}


@Composable
private fun PlayersScreen(app: AppState) {
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
    HorizontalDivider()
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

@Composable
private fun SettingsScreen(@Suppress("UNUSED_PARAMETER") app: AppState) {
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

@Composable
private fun NewGameScreen(app: AppState) {
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
    Button(onClick = { app.createGame(count) }, enabled = app.players.size >= count) {
      Text("Create Game")
    }
    Spacer(Modifier.height(8.dp))
    Text("Players used will be the first $count from your Players list.", color = Color.Gray)
  }
}

@Composable
private fun HistoryScreen(app: AppState) {
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
        modifier = Modifier.fillMaxWidth().clickable { app.navigate(Screen.GameEditor(game.id)) },
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

@Composable
private fun GameEditorScreen(app: AppState, gameId: Int) {
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
    HorizontalDivider()
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

@Composable
private fun RoundEditor(game: Game, onAdd: (Round) -> Unit) {
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
