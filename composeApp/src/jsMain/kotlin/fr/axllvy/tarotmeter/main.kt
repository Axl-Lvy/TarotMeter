package fr.axllvy.tarotmeter

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import fr.axllvy.tarotmeter.util.getInitialRouteFromUrl
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
  onWasmReady {
    startKoin { modules(*initKoinModules()) }
    val body = document.body ?: return@onWasmReady
    ComposeViewport(body) {
      App(
        initialRoute = getInitialRouteFromUrl(),
        onNavHostReady = { it.bindToBrowserNavigation() },
      )
    }
  }
}
