package fr.axllvy.tarotmeter

import androidx.compose.runtime.*
import androidx.navigation.NavController
import fr.axllvy.tarotmeter.core.data.cloud.auth.AuthManager
import fr.axllvy.tarotmeter.core.data.config.LANGUAGE_SETTING
import fr.axllvy.tarotmeter.core.localization.Localization
import fr.axllvy.tarotmeter.ui.AppScaffold
import fr.axllvy.tarotmeter.ui.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App(initialRoute: String? = null, onNavHostReady: suspend (NavController) -> Unit = {}) {
  // Apply saved language on app startup
  val localization = koinInject<Localization>()
  val authManager = koinInject<AuthManager>()
  authManager.initialize()

  LaunchedEffect(LANGUAGE_SETTING.value) {
    val languageToApply =
      if (LANGUAGE_SETTING.value == "und") {
        // Use system default language when set to "und" (Default)
        localization.getSystemLanguage()
      } else {
        LANGUAGE_SETTING.value
      }
    localization.applyLanguage(languageToApply)
  }

  AppTheme { AppScaffold(initialRoute = initialRoute, onNavHostReady = onNavHostReady) }
}
