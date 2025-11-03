package proj.tarotmeter.axl.ui

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import proj.tarotmeter.axl.core.data.config.LANGUAGE_SETTING
import proj.tarotmeter.axl.core.localization.Language
import proj.tarotmeter.axl.core.localization.Localization
import proj.tarotmeter.axl.ui.components.ButtonRow
import proj.tarotmeter.axl.ui.components.SegmentedButtons
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
