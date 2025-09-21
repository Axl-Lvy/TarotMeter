package proj.tarotmeter.axl.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import proj.tarotmeter.axl.AppState

/**
 * Screen for application settings. Allows toggling dark mode and hints.
 *
 * @param app The application state
 */
@Composable
fun SettingsScreen(@Suppress("UNUSED_PARAMETER") app: AppState) {
  var darkMode by remember { mutableStateOf(true) }
  var showTips by remember { mutableStateOf(true) }
  Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("Common Settings", style = MaterialTheme.typography.titleMedium)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      Text("Dark Mode", modifier = Modifier.weight(1f))
      Switch(checked = darkMode, onCheckedChange = { darkMode = it })
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      Text("Show Hints", modifier = Modifier.weight(1f))
      Switch(checked = showTips, onCheckedChange = { showTips = it })
    }
    Text("These are placeholders. Persist them later with your database.", color = Color.Gray)
  }
}
