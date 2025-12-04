package fr.axllvy.tarotmeter.ui.pages

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import fr.axllvy.tarotmeter.core.data.config.LANGUAGE_SETTING
import fr.axllvy.tarotmeter.core.localization.Language
import fr.axllvy.tarotmeter.core.localization.Localization
import fr.axllvy.tarotmeter.ui.components.ButtonRow
import fr.axllvy.tarotmeter.ui.components.SegmentedButtons
import org.jetbrains.compose.resources.stringResource
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.settings_language
import tarotmeter.composeapp.generated.resources.settings_language_description

@Composable
actual fun LanguageSelection(selectedLanguage: Language, localization: Localization) {
  HorizontalDivider()
  ButtonRow(
    title = stringResource(Res.string.settings_language),
    subTitle = stringResource(Res.string.settings_language_description),
  ) {
    SegmentedButtons(
      Language.entries.map { stringResource(it.displayName) },
      Language.entries.indexOf(selectedLanguage),
      onSelect = { index ->
        val newLanguage = Language.entries[index]
        if (newLanguage == Language.Default) {
          // Reset to system default language
          val systemLanguage = localization.getSystemLanguage()
          LANGUAGE_SETTING.value = "und"
          localization.applyLanguage(systemLanguage)
        } else {
          LANGUAGE_SETTING.value = newLanguage.iso
          localization.applyLanguage(newLanguage.iso)
        }
      },
      key = "language_selector",
    )
  }
}
