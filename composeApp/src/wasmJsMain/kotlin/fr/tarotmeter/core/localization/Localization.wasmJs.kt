package fr.tarotmeter.core.localization

import kotlinx.browser.window

/** WASM implementation of localization handler. */
actual class Localization {
  /**
   * Apply the language to WASM's locale system. Note: WASM locale support is limited and may
   * require browser refresh.
   */
  actual fun applyLanguage(iso: String) {
    // WASM doesn't have direct locale support like JVM or Android
    // The locale is primarily controlled by browser settings
    // We'll just store the preference and let Compose resources handle it
  }

  /** Get the system's default language ISO code. */
  actual fun getSystemLanguage(): String {
    return window.navigator.language.substringBefore("-")
  }
}
