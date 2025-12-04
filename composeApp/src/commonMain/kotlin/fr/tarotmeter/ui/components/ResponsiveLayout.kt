package fr.tarotmeter.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Enum representing different screen size categories for responsive design. */
enum class WindowSize {
  COMPACT, // Phone portrait
  MEDIUM, // Phone landscape, small tablet
  EXPANDED, // Large tablet, desktop
}

/**
 * Determines the current window size based on available width.
 *
 * @param width The available width in dp
 * @return The corresponding WindowSize
 */
fun getWindowSize(width: Int): WindowSize {
  return when {
    width < 600 -> WindowSize.COMPACT
    width < 840 -> WindowSize.MEDIUM
    else -> WindowSize.EXPANDED
  }
}

/**
 * A responsive container that adapts padding and max width based on screen size.
 *
 * @param modifier Modifier to be applied to the container
 * @param content The content to display
 */
@Composable
fun ResponsiveContainer(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val windowSize = getWindowSize(maxWidth.value.toInt())
    val horizontalPadding =
      when (windowSize) {
        WindowSize.COMPACT -> 16.dp
        WindowSize.MEDIUM -> 32.dp
        WindowSize.EXPANDED -> 48.dp
      }

    Box(
      modifier =
        Modifier.fillMaxSize()
          .padding(horizontal = horizontalPadding, vertical = 16.dp)
          .then(
            if (windowSize == WindowSize.EXPANDED) Modifier.widthIn(max = 1200.dp) else Modifier
          )
    ) {
      content()
    }
  }
}

/**
 * A responsive two-column layout that stacks on narrow screens.
 *
 * @param modifier Modifier to be applied
 * @param leftContent Content for the left column
 * @param rightContent Content for the right column
 */
@Composable
fun ResponsiveTwoColumn(
  modifier: Modifier = Modifier,
  leftContent: @Composable ColumnScope.() -> Unit,
  rightContent: @Composable ColumnScope.() -> Unit,
) {
  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val windowSize = getWindowSize(maxWidth.value.toInt())

    if (windowSize == WindowSize.COMPACT) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Column(Modifier.fillMaxWidth()) { leftContent() }
        Column(Modifier.fillMaxWidth()) { rightContent() }
      }
    } else {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f)) { leftContent() }
        Column(Modifier.weight(1f)) { rightContent() }
      }
    }
  }
}
