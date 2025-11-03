package proj.tarotmeter.axl

import androidx.compose.runtime.*
import androidx.navigation.NavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import proj.tarotmeter.axl.core.data.config.LANGUAGE_SETTING
import proj.tarotmeter.axl.core.localization.Localization
import proj.tarotmeter.axl.ui.AppScaffold
import proj.tarotmeter.axl.ui.theme.AppTheme

@Composable
@Preview
fun App(onNavHostReady: suspend (NavController) -> Unit = {}) {
  val modules = initKoinModules()
  KoinApplication(application = { modules(*modules) }) {
    // Apply saved language on app startup
    val localization = koinInject<Localization>()

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

    AppTheme { AppScaffold(onNavHostReady) }
  }
}
