package proj.tarotmeter.axl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import proj.tarotmeter.axl.core.data.config.APP_THEME_SETTING
import proj.tarotmeter.axl.ui.components.CustomElevatedCard
import proj.tarotmeter.axl.ui.components.ResponsiveContainer
import proj.tarotmeter.axl.ui.components.SectionHeader
import proj.tarotmeter.axl.ui.components.SegmentedButtons
import proj.tarotmeter.axl.ui.theme.AppThemeSetting

/** Screen for application settings. Allows choosing theme and toggling hints. */
@Composable
fun SettingsScreen() {
  var showTips by remember { mutableStateOf(true) }

  ResponsiveContainer {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      SectionHeader("Settings")

      CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Text("Appearance", style = MaterialTheme.typography.titleMedium)

          ButtonRow("App theme", "Choose light, dark, or system default theme.") {
            SegmentedButtons(
              AppThemeSetting.entries.map { it.displayName },
              AppThemeSetting.entries.indexOf(APP_THEME_SETTING.value),
              onSelect = { index -> APP_THEME_SETTING.value = AppThemeSetting.entries[index] },
            )
          }

          HorizontalDivider()

          ButtonRow(title = "Show Tips", subTitle = "Enable or disable in-app tips and hints.") {
            Switch(checked = showTips, onCheckedChange = { showTips = it })
          }
        }
      }

      CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("About", style = MaterialTheme.typography.titleMedium)
          Text(
            "Tarot Meter is a comprehensive score tracking application for French Tarot games.",
            style = MaterialTheme.typography.bodyMedium,
          )
          Text(
            "Version 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          "Theme settings are persisted. Other settings will be added in future versions.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.padding(12.dp),
        )
      }
    }
  }
}

@Composable
private fun ButtonRow(title: String, subTitle: String, content: @Composable () -> Unit) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.bodyLarge)
      Text(
        subTitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Box(modifier = Modifier.weight(1f)) { content() }
  }
}
