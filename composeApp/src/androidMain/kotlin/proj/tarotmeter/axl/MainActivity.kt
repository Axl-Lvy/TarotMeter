package proj.tarotmeter.axl

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

lateinit var MAIN_ACTIVITY: MainActivity

class MainActivity : ComponentActivity() {
  init {
    MAIN_ACTIVITY = this
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    val initialRoute = handleDeepLink(intent)

    setContent { App(initialRoute = initialRoute) }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    // Handle deeplink when app is already running
    handleDeepLink(intent)?.let { route ->
      // Navigation will be handled via onNavHostReady callback in App
      recreate()
    }
  }

  private fun handleDeepLink(intent: Intent): String? {
    val data: Uri? = intent.data
    return if (intent.action == Intent.ACTION_VIEW && data != null) {
      parseDeepLink(data)
    } else {
      null
    }
  }

  private fun parseDeepLink(uri: Uri): String? {
    // Expected format: https://www.axl-lvy.fr/tarotmeter#confirm-email/<token>
    val fragment = uri.fragment
    if (fragment != null && fragment.startsWith("confirm-email/")) {
      val token = fragment.removePrefix("confirm-email/")
      if (token.isNotEmpty()) {
        return "confirm-email/$token"
      }
    }
    return null
  }
}

@Preview
@Composable
fun AppAndroidPreview() {
  App()
}
