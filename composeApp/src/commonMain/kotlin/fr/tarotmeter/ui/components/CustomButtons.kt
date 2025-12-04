package fr.tarotmeter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Primary action button with consistent styling.
 *
 * @param text The button text
 * @param onClick Callback when button is clicked
 * @param modifier Modifier to be applied to the button
 * @param enabled Whether the button is enabled
 * @param maxLines Maximum number of lines for the button text
 */
@Composable
fun PrimaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  maxLines: Int = Int.MAX_VALUE,
) {
  Button(
    onClick = onClick,
    modifier = modifier.height(56.dp),
    shape = RoundedCornerShape(12.dp),
    enabled = enabled,
  ) {
    Text(text, style = MaterialTheme.typography.titleMedium, maxLines = maxLines)
  }
}

/**
 * Secondary action button with outlined styling.
 *
 * @param text The button text
 * @param onClick Callback when button is clicked
 * @param modifier Modifier to be applied to the button
 * @param enabled Whether the button is enabled
 */
@Composable
fun SecondaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier.height(56.dp),
    shape = RoundedCornerShape(12.dp),
    enabled = enabled,
  ) {
    Text(text, style = MaterialTheme.typography.titleMedium)
  }
}
