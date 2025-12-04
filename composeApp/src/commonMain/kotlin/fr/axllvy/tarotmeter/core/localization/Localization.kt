package fr.axllvy.tarotmeter.core.localization

/** Platform-specific localization handler. Updates the app's locale. */
expect class Localization {
  /** Apply the language to the platform's locale system. */
  fun applyLanguage(iso: String)

  /** Get the system's default language ISO code. */
  fun getSystemLanguage(): String
}
