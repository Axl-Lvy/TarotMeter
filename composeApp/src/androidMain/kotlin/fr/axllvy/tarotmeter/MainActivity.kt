package fr.axllvy.tarotmeter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import fr.axllvy.tarotmeter.util.parseDeepLink

lateinit var MAIN_ACTIVITY: MainActivity

class MainActivity : ComponentActivity() {
  init {
    MAIN_ACTIVITY = this
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    val initialRoute = parseDeepLink(intent)

    setContent { App(initialRoute = initialRoute) }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    // Handle deeplink when app is already running
    parseDeepLink(intent)?.let {
      // Navigation will be handled via onNavHostReady callback in App
      recreate()
    }
  }
}

@Preview
@Composable
fun AppAndroidPreview() {
  App()
}
