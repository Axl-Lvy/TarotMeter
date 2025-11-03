package proj.tarotmeter.axl.core.localization

import kotlinx.browser.window

/** JavaScript implementation of localization handler. */
actual class Localization {
  /**
   * Apply the language to JavaScript's locale system. Note: JS locale support is limited and may
   * require page refresh.
   */
  actual fun applyLanguage(iso: String) {
    // JavaScript doesn't have direct locale support like JVM or Android
    // The locale is primarily controlled by browser settings
    // We'll just store the preference and let Compose resources handle it
  }

  /** Get the system's default language ISO code. */
  actual fun getSystemLanguage(): String {
    return window.navigator.language.substringBefore("-")
  }
}
