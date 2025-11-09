package proj.tarotmeter.axl.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.ArrowAltCircleLeft
import org.jetbrains.compose.resources.stringResource
import proj.tarotmeter.axl.ui.navigation.Route
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.cd_navigate_back

/**
 * A back button that navigates back.
 *
 * @param route The current navigation route
 * @param onBack Callback for when the back button is pressed
 */
@Composable
fun BackButton(route: String, onBack: () -> Unit) {
  if (route != Route.Home.route) {
    IconButton(onClick = onBack) {
      Icon(
        imageVector = FontAwesomeIcons.Regular.ArrowAltCircleLeft,
        contentDescription = stringResource(Res.string.cd_navigate_back),
      )
    }
  } else {
    HomeBackButton()
  }
}

/** Back button shown on the home screen. */
@Composable expect fun HomeBackButton()
