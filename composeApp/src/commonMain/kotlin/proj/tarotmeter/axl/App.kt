package proj.tarotmeter.axl

import androidx.compose.runtime.*
import androidx.navigation.NavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import proj.tarotmeter.axl.ui.AppScaffold
import proj.tarotmeter.axl.ui.theme.AppTheme

@Composable
@Preview
fun App(onNavHostReady: suspend (NavController) -> Unit = {}) {
  val modules = initKoinModules()
  KoinApplication(application = { modules(*modules) }) { AppTheme { AppScaffold(onNavHostReady) } }
}
