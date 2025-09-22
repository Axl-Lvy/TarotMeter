package proj.tarotmeter.axl

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import proj.tarotmeter.axl.ui.AppScaffold

@Composable
@Preview
fun App() {
  val modules = initKoinModules()
  KoinApplication(application = { modules(*modules) }) { MaterialTheme { AppScaffold() } }
}
