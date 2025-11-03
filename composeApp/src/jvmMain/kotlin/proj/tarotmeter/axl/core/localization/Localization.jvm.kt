package proj.tarotmeter.axl.core.localization

import java.util.Locale

/** JVM implementation of localization handler. */
actual class Localization {
  /** Apply the language to JVM's locale system. */
  actual fun applyLanguage(iso: String) {
    val locale = Locale.forLanguageTag(iso)
    Locale.setDefault(locale)
  }

  /** Get the system's default language ISO code. */
  actual fun getSystemLanguage(): String {
    return Locale.getDefault().language
  }
}
