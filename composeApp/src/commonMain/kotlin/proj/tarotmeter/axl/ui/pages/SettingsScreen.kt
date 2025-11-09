package proj.tarotmeter.axl.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import proj.tarotmeter.axl.core.data.cloud.auth.AuthManager
import proj.tarotmeter.axl.core.data.config.APP_THEME_SETTING
import proj.tarotmeter.axl.core.data.config.LANGUAGE_SETTING
import proj.tarotmeter.axl.core.localization.Language
import proj.tarotmeter.axl.core.localization.Localization
import proj.tarotmeter.axl.ui.components.ButtonRow
import proj.tarotmeter.axl.ui.components.CustomElevatedCard
import proj.tarotmeter.axl.ui.components.SectionHeader
import proj.tarotmeter.axl.ui.components.SegmentedButtons
import proj.tarotmeter.axl.ui.components.SignInButton
import proj.tarotmeter.axl.ui.components.SignUpButton
import proj.tarotmeter.axl.ui.theme.AppThemeSetting
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.settings_about
import tarotmeter.composeapp.generated.resources.settings_about_description
import tarotmeter.composeapp.generated.resources.settings_account
import tarotmeter.composeapp.generated.resources.settings_app_theme
import tarotmeter.composeapp.generated.resources.settings_app_theme_description
import tarotmeter.composeapp.generated.resources.settings_appearance
import tarotmeter.composeapp.generated.resources.settings_version

/** Screen for application settings. Allows choosing theme and toggling hints. */
@Composable
fun SettingsScreen() {
  val localization = koinInject<Localization>()
  val authManager = koinInject<AuthManager>()
  val selectedLanguage by derivedStateOf { Language.fromIso(LANGUAGE_SETTING.value) }
  val scrollState = rememberScrollState()
  val isSignedIn = authManager.user != null

  // Force recomposition when language changes
  key(LANGUAGE_SETTING.value) {
    Column(
      Modifier.fillMaxSize().verticalScroll(scrollState),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          SectionHeader(stringResource(Res.string.settings_appearance))

          ButtonRow(
            stringResource(Res.string.settings_app_theme),
            stringResource(Res.string.settings_app_theme_description),
          ) {
            SegmentedButtons(
              AppThemeSetting.entries.map { stringResource(it.displayName) },
              AppThemeSetting.entries.indexOf(APP_THEME_SETTING.value),
              onSelect = { index -> APP_THEME_SETTING.value = AppThemeSetting.entries[index] },
              key = "app_theme_selector",
            )
          }

          LanguageSelection(selectedLanguage, localization)
        }
      }

      CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          SectionHeader(stringResource(Res.string.settings_about))
          Text(
            stringResource(Res.string.settings_about_description),
            style = MaterialTheme.typography.bodyMedium,
          )
          Text(
            stringResource(Res.string.settings_version),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          SectionHeader(stringResource(Res.string.settings_account))
          SignInButton()
          if (!isSignedIn) {
            SignUpButton()
          }
        }
      }
    }
  }
}

@Composable expect fun LanguageSelection(selectedLanguage: Language, localization: Localization)
