package fr.tarotmeter.core.localization

import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults

/** iOS implementation of localization handler. */
actual class Localization {
  /** Apply the language to iOS's locale system. */
  actual fun applyLanguage(iso: String) {
    NSUserDefaults.standardUserDefaults.setObject(arrayListOf(iso), "AppleLanguages")
  }

  /** Get the system's default language ISO code. */
  actual fun getSystemLanguage(): String {
    return NSLocale.preferredLanguages.firstOrNull()?.toString()?.substringBefore("-") ?: "en"
  }
}
