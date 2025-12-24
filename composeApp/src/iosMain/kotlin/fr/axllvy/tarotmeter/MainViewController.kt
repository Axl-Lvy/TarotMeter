package fr.axllvy.tarotmeter

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.context.startKoin

private var isKoinInitialized = false

fun MainViewController() = ComposeUIViewController {
  if (!isKoinInitialized) {
    startKoin { modules(*initKoinModules()) }
    isKoinInitialized = true
  }
  App()
}
