package proj.tarotmeter.axl.core.localization

import android.content.Context
import android.os.LocaleList
import java.util.Locale

/** Android implementation of localization handler. */
actual class Localization(private val context: Context) {
  /** Apply the language to Android's locale system. */
  actual fun applyLanguage(iso: String) {
    val locale = Locale.forLanguageTag(iso)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocales(LocaleList(locale))
  }

  /** Get the system's default language ISO code. */
  actual fun getSystemLanguage(): String {
    return context.resources.configuration.locales[0].language
  }
}
