package fr.tarotmeter.ui.pages

import androidx.compose.runtime.Composable
import fr.tarotmeter.core.localization.Language
import fr.tarotmeter.core.localization.Localization

@Composable
actual fun LanguageSelection(selectedLanguage: Language, localization: Localization) {
  // No-op in web version
}
