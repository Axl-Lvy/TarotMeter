package fr.tarotmeter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Displays a player avatar with their initials.
 *
 * @param name The player's name
 * @param modifier Modifier to be applied to the avatar
 * @param size Size of the avatar circle
 */
@Composable
fun PlayerAvatar(name: String, modifier: Modifier = Modifier, size: Dp = 40.dp) {
  val initials =
    name
      .split(' ')
      .mapNotNull { it.firstOrNull()?.uppercase() }
      .take(2)
      .joinToString("")
      .ifEmpty { "?" }

  Box(
    modifier =
      modifier.size(size).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = initials,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onPrimaryContainer,
    )
  }
}

/**
 * Displays a score with colored styling based on positive/negative value.
 *
 * @param score The score to display
 * @param modifier Modifier to be applied
 */
@Composable
fun ScoreText(score: Int, modifier: Modifier = Modifier) {
  val color =
    when {
      score > 0 -> Color(0xFF2E7D32)
      score < 0 -> Color(0xFFC62828)
      else -> MaterialTheme.colorScheme.onSurface
    }

  Text(
    text = if (score >= 0) "+$score" else "$score",
    style = MaterialTheme.typography.titleLarge,
    fontWeight = FontWeight.Bold,
    color = color,
    modifier = modifier,
  )
}

/**
 * Displays player scores in a horizontal layout.
 *
 * @param playerScores Map of player names to their scores
 * @param modifier Modifier to be applied
 */
@Composable
fun PlayerScoresRow(playerScores: List<Pair<String, Int>>, modifier: Modifier = Modifier) {
  CustomElevatedCard(modifier = modifier.fillMaxWidth()) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
      playerScores.forEach { (name, score) ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(Modifier.height(4.dp))
          ScoreText(score = score)
        }
      }
    }
  }
}
