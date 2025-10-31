package proj.tarotmeter.axl.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A styled card component with consistent elevation and shape used throughout the app.
 *
 * @param modifier Modifier to be applied to the card
 * @param onClick Optional click handler for the card
 * @param content The content to display inside the card
 */
@Composable
fun CustomElevatedCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  if (onClick != null) {
    Card(
      onClick = onClick,
      modifier = modifier,
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
      Column(Modifier.padding(16.dp)) { content() }
    }
  } else {
    Card(
      modifier = modifier,
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
      Column(Modifier.padding(16.dp)) { content() }
    }
  }
}
