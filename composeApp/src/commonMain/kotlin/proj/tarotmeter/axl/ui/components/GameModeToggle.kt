package proj.tarotmeter.axl.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import proj.tarotmeter.axl.ui.components.GameScreenTab.AddGame
import proj.tarotmeter.axl.ui.components.GameScreenTab.Stats
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.game_editor_toggle_add_game
import tarotmeter.composeapp.generated.resources.game_editor_toggle_stats

/** Enum representing the tabs available in the game screen. */
enum class GameScreenTab {
  AddGame,
  Stats,
}

/**
 * A toggle component for switching between game screen tabs.
 *
 * @param selectedTab The currently selected tab
 * @param onTabSelected Callback when a tab is selected
 * @param modifier Modifier for the component
 */
@Composable
fun GameModeToggle(
  selectedTab: GameScreenTab,
  onTabSelected: (GameScreenTab) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    ModeChip(
      text = stringResource(Res.string.game_editor_toggle_add_game),
      selected = selectedTab == AddGame,
      onClick = { onTabSelected(AddGame) },
      modifier = Modifier.weight(1f),
    )
    ModeChip(
      text = stringResource(Res.string.game_editor_toggle_stats),
      selected = selectedTab == Stats,
      onClick = { onTabSelected(Stats) },
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun ModeChip(
  text: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    onClick = onClick,
    shape = RoundedCornerShape(999.dp),
    color =
      if (selected) {
        MaterialTheme.colorScheme.primary
      } else {
        MaterialTheme.colorScheme.surface
      },
    border =
      if (selected) {
        null
      } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
      },
    tonalElevation = if (selected) 2.dp else 0.dp,
  ) {
    Text(
      text = text,
      modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
      style = MaterialTheme.typography.labelLarge,
      textAlign = TextAlign.Center,
      color =
        if (selected) {
          MaterialTheme.colorScheme.onPrimary
        } else {
          MaterialTheme.colorScheme.onSurface
        },
    )
  }
}
