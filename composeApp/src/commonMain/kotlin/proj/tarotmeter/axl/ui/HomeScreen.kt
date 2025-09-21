package proj.tarotmeter.axl.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * The home screen of the application. Provides navigation buttons to other screens and a brief app
 * introduction.
 *
 * @param onNewGame Callback for creating a new game
 * @param onPlayers Callback for navigating to the players screen
 * @param onHistory Callback for navigating to the game history screen
 * @param onSettings Callback for navigating to the settings screen
 */
@Composable
fun HomeScreen(
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
