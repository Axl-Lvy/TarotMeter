package proj.tarotmeter.axl

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import kotlinx.browser.document
import proj.tarotmeter.axl.util.getInitialRouteFromUrl

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
  val body = document.body ?: return
  ComposeViewport(body) {
    App(initialRoute = getInitialRouteFromUrl(), onNavHostReady = { it.bindToBrowserNavigation() })
  }
}
