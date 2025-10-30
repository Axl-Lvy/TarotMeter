package proj.tarotmeter.axl.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import proj.tarotmeter.axl.core.data.config.APP_THEME_SETTING

@Composable
fun AppTheme(app: @Composable () -> Unit) {
  val appThemeSetting = APP_THEME_SETTING.value
  val darkTheme =
    when (appThemeSetting) {
      AppThemeSetting.LIGHT -> false
      AppThemeSetting.DARK -> true
      AppThemeSetting.SYSTEM -> isSystemInDarkTheme()
    }
  MaterialTheme(colorScheme = getColorScheme(darkTheme)) { app() }
}
