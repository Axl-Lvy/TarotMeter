package proj.tarotmeter.axl.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.settings_app_theme_dark
import tarotmeter.composeapp.generated.resources.settings_app_theme_light
import tarotmeter.composeapp.generated.resources.settings_app_theme_system

@Composable
fun getColorScheme(darkTheme: Boolean): ColorScheme {
  return if (darkTheme) darkColorScheme else lightColorScheme
}

enum class AppThemeSetting(val displayName: StringResource) {
  SYSTEM(Res.string.settings_app_theme_system),
  LIGHT(Res.string.settings_app_theme_light),
  DARK(Res.string.settings_app_theme_dark),
}
