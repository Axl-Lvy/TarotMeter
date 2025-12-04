package fr.tarotmeter.ui.components

import androidx.compose.foundation.Image
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import org.jetbrains.compose.resources.painterResource
import tarotmeter.composeapp.generated.resources.A
import tarotmeter.composeapp.generated.resources.Res

private const val MAIN_WEBSITE_URL = "https://www.axl-lvy.fr"

/** Back button shown on the home screen. It redirects to the main website. */
@Composable
actual fun HomeBackButton() {
  val uriHandler = LocalUriHandler.current
  IconButton(onClick = { uriHandler.openUri(MAIN_WEBSITE_URL) }) {
    Image(painter = painterResource(Res.drawable.A), contentDescription = "Axl-Lvy Logo")
  }
}
