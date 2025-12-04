package fr.tarotmeter.util

import org.koin.core.component.KoinComponent

abstract class InitializableKoinComponent : KoinComponent {

  private var isInitialized: Boolean = false

  /** Force this instance to initialize, even if it normally initializes lazily. */
  fun initialize() {
    if (!isInitialized) {
      isInitialized = true
    }
  }
}
