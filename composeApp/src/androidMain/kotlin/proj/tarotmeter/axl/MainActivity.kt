package proj.tarotmeter.axl

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

    setContent { App() }
  }
}

@Preview
@Composable
fun AppAndroidPreview() {
  App()
}
