package proj.tarotmeter.axl.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.SimpleIcons
import compose.icons.simpleicons.Github
import proj.tarotmeter.axl.ui.components.PrimaryButton
import proj.tarotmeter.axl.ui.components.ResponsiveContainer
import proj.tarotmeter.axl.ui.components.SecondaryButton

private const val GITHUB_REPO_URL = "https://github.com/Axl-Lvy/TarotMeter"

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
    Brush.verticalGradient(
      listOf(
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surface,
      )
    )

  Box(modifier = Modifier.fillMaxSize().background(gradient)) {
    ResponsiveContainer {
      Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Spacer(Modifier.height(32.dp))

        // Header
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            "Tarot Meter",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )
          Spacer(Modifier.height(8.dp))
          Text(
            "Track your Tarot games with elegance",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        // Navigation buttons
        Column(
          Modifier.fillMaxWidth().widthIn(max = 400.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          PrimaryButton(text = "New Game", onClick = onNewGame, modifier = Modifier.fillMaxWidth())
          SecondaryButton(text = "Players", onClick = onPlayers, modifier = Modifier.fillMaxWidth())
          SecondaryButton(
            text = "Game History",
            onClick = onHistory,
            modifier = Modifier.fillMaxWidth(),
          )
          SecondaryButton(
            text = "Settings",
            onClick = onSettings,
            modifier = Modifier.fillMaxWidth(),
          )
        }

        Footer()
      }
    }
  }
}

/** The footer section of the home screen, containing app credits and a link to the GitHub repo. */
@Composable
private fun Footer() {
  val uriHandler = LocalUriHandler.current
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.padding(bottom = 16.dp),
  ) {
    Text(
      "No adds. Open source. Will remain as such.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(4.dp))
    IconButton(onClick = { uriHandler.openUri(GITHUB_REPO_URL) }) {
      Icon(imageVector = SimpleIcons.Github, contentDescription = "GitHub Repository")
    }
  }
}
