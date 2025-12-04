package fr.tarotmeter.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.tarotmeter.core.data.model.GameSource
import org.jetbrains.compose.resources.stringResource
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.game_source_local
import tarotmeter.composeapp.generated.resources.game_source_remote

/**
 * Badge component to display the source of a game (local or remote/shared).
 *
 * @param source The source of the game
 * @param modifier Optional modifier for the badge
 */
@Composable
fun GameSourceBadge(source: GameSource, modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.small,
    color =
      when (source) {
        GameSource.LOCAL -> MaterialTheme.colorScheme.secondaryContainer
        GameSource.REMOTE -> MaterialTheme.colorScheme.tertiaryContainer
      },
  ) {
    Text(
      text =
        when (source) {
          GameSource.LOCAL -> stringResource(Res.string.game_source_local)
          GameSource.REMOTE -> stringResource(Res.string.game_source_remote)
        },
      style = MaterialTheme.typography.labelSmall,
      color =
        when (source) {
          GameSource.LOCAL -> MaterialTheme.colorScheme.onSecondaryContainer
          GameSource.REMOTE -> MaterialTheme.colorScheme.onTertiaryContainer
        },
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    )
  }
}
