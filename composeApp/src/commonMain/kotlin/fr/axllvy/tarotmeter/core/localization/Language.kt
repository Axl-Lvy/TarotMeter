package fr.axllvy.tarotmeter.core.localization

import org.jetbrains.compose.resources.StringResource
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.settings_language_default
import tarotmeter.composeapp.generated.resources.settings_language_english
import tarotmeter.composeapp.generated.resources.settings_language_french

/** Represents supported languages in the application. */
enum class Language(val iso: String, val displayName: StringResource) {
  Default(iso = "und", displayName = Res.string.settings_language_default),
  English(iso = "en", displayName = Res.string.settings_language_english),
  French(iso = "fr", displayName = Res.string.settings_language_french);

  companion object {
    /** Get language by ISO code, defaulting to Default if not found. */
    fun fromIso(iso: String): Language = entries.firstOrNull { it.iso == iso } ?: Default
  }
}
