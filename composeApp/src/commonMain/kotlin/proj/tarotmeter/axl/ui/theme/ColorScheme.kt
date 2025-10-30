package proj.tarotmeter.axl.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
fun getColorScheme(darkTheme: Boolean): ColorScheme {
  return if (darkTheme) darkColorScheme else lightColorScheme
}

enum class AppThemeSetting(val displayName: String) {
  SYSTEM("System"),
  LIGHT("Light"),
  DARK("Dark"),
}
