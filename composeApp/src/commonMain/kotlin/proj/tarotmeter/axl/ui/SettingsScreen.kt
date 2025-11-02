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
import org.jetbrains.compose.resources.stringResource
import proj.tarotmeter.axl.core.data.config.APP_THEME_SETTING
import proj.tarotmeter.axl.ui.components.CustomElevatedCard
import proj.tarotmeter.axl.ui.components.ResponsiveContainer
import proj.tarotmeter.axl.ui.components.SectionHeader
import proj.tarotmeter.axl.ui.components.SegmentedButtons
import proj.tarotmeter.axl.ui.theme.AppThemeSetting
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.*

/** Screen for application settings. Allows choosing theme and toggling hints. */
@Composable
fun SettingsScreen() {
  var showTips by remember { mutableStateOf(true) }

  ResponsiveContainer {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      SectionHeader(stringResource(Res.string.settings_header))

      CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Text(stringResource(Res.string.settings_appearance), style = MaterialTheme.typography.titleMedium)

          ButtonRow(stringResource(Res.string.settings_app_theme), stringResource(Res.string.settings_app_theme_description)) {
            SegmentedButtons(
              AppThemeSetting.entries.map { it.displayName },
              AppThemeSetting.entries.indexOf(APP_THEME_SETTING.value),
              onSelect = { index -> APP_THEME_SETTING.value = AppThemeSetting.entries[index] },
            )
          }

          HorizontalDivider()

          ButtonRow(title = stringResource(Res.string.settings_show_tips), subTitle = stringResource(Res.string.settings_show_tips_description)) {
            Switch(checked = showTips, onCheckedChange = { showTips = it })
          }
        }
      }

      CustomElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(stringResource(Res.string.settings_about), style = MaterialTheme.typography.titleMedium)
          Text(
            stringResource(Res.string.settings_about_description),
            style = MaterialTheme.typography.bodyMedium,
          )
          Text(
            stringResource(Res.string.settings_version),
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
          stringResource(Res.string.settings_persistence_note),
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
