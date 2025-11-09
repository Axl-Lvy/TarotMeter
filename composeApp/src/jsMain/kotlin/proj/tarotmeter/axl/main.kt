package proj.tarotmeter.axl

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady
import proj.tarotmeter.axl.util.getInitialRouteFromUrl

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
  onWasmReady {
    val body = document.body ?: return@onWasmReady
    ComposeViewport(body) {
      App(
        initialRoute = getInitialRouteFromUrl(),
        onNavHostReady = { it.bindToBrowserNavigation() },
      )
    }
  }
}
