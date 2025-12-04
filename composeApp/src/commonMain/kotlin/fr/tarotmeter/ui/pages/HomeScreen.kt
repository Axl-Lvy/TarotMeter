package fr.tarotmeter.ui.pages

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
import fr.tarotmeter.ui.components.PrimaryButton
import fr.tarotmeter.ui.components.ResponsiveContainer
import fr.tarotmeter.ui.components.SecondaryButton
import org.jetbrains.compose.resources.stringResource
import tarotmeter.composeapp.generated.resources.*
import tarotmeter.composeapp.generated.resources.Res

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
            stringResource(Res.string.title_home),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )
          Spacer(Modifier.height(8.dp))
          Text(
            stringResource(Res.string.home_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        // Navigation buttons
        Column(
          Modifier.fillMaxWidth().widthIn(max = 400.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          PrimaryButton(
            text = stringResource(Res.string.home_button_new_game),
            onClick = onNewGame,
            modifier = Modifier.fillMaxWidth(),
          )
          SecondaryButton(
            text = stringResource(Res.string.home_button_players),
            onClick = onPlayers,
            modifier = Modifier.fillMaxWidth(),
          )
          SecondaryButton(
            text = stringResource(Res.string.home_button_history),
            onClick = onHistory,
            modifier = Modifier.fillMaxWidth(),
          )
          SecondaryButton(
            text = stringResource(Res.string.home_button_settings),
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
      stringResource(Res.string.home_footer_text),
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(4.dp))
    IconButton(onClick = { uriHandler.openUri(GITHUB_REPO_URL) }) {
      Icon(
        imageVector = SimpleIcons.Github,
        contentDescription = stringResource(Res.string.cd_github_repository),
      )
    }
  }
}
