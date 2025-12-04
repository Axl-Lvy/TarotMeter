package fr.axllvy.tarotmeter.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.StringResource
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.settings_app_theme_dark
import tarotmeter.composeapp.generated.resources.settings_app_theme_light
import tarotmeter.composeapp.generated.resources.settings_app_theme_system

@Composable
fun getColorScheme(darkTheme: Boolean): ColorScheme {
  return if (darkTheme) darkColorScheme else lightColorScheme
}

val GOOD_TINT = Color(0xFF4CAF50)

enum class AppThemeSetting(val displayName: StringResource) {
  SYSTEM(Res.string.settings_app_theme_system),
  LIGHT(Res.string.settings_app_theme_light),
  DARK(Res.string.settings_app_theme_dark),
}
