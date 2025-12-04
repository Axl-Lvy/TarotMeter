package fr.tarotmeter

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.ExperimentalBrowserHistoryApi
import androidx.navigation.bindToBrowserNavigation
import fr.tarotmeter.util.getInitialRouteFromUrl
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class, ExperimentalBrowserHistoryApi::class)
fun main() {
  val body = document.body ?: return
  ComposeViewport(body) {
    App(initialRoute = getInitialRouteFromUrl(), onNavHostReady = { it.bindToBrowserNavigation() })
  }
}
