package proj.tarotmeter.axl.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Global holder for scroll states that need to persist across recompositions. This allows scroll
 * positions to survive key-based recompositions.
 */
private object ScrollStateHolder {
  private val states = mutableMapOf<String, ScrollState>()

  fun getOrCreate(key: String): ScrollState {
    return states.getOrPut(key) { ScrollState(0) }
  }
}

/**
 * A dropdown selector component with consistent styling.
 *
 * @param label Label for the dropdown
 * @param options List of options to choose from
 * @param selectedIndex Currently selected index
 * @param onSelect Callback when an option is selected
 * @param modifier Modifier to be applied to the dropdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotDropdown(
  label: String,
  options: List<String>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  val selectedOption = options.getOrNull(selectedIndex) ?: ""

  ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = it },
    modifier = modifier,
  ) {
    OutlinedTextField(
      value = selectedOption,
      onValueChange = {},
      readOnly = true,
      label = { Text(label) },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
      colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
      modifier =
        Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
    )
    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      options.forEachIndexed { index, option ->
        DropdownMenuItem(
          text = { Text(option) },
          onClick = {
            onSelect(index)
            expanded = false
          },
          contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
        )
      }
    }
  }
}

/**
 * A segmented button group for selecting between multiple options.
 *
 * @param options List of options to display as selectable segments.
 * @param selectedIndex The index of the currently selected option.
 * @param onSelect Callback invoked when an option is selected, with the selected index.
 * @param modifier Modifier to be applied to the segmented button group.
 * @param key Optional key to preserve scroll state across recompositions.
 * @param key Optional key to preserve scroll state across recompositions
 */
@Composable
fun SegmentedButtons(
  options: List<String>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit,
  modifier: Modifier = Modifier,
  key: String? = null,
) {
  val scrollState =
    remember(key) {
      if (key != null) {
        ScrollStateHolder.getOrCreate(key)
      } else {
        ScrollState(0)
      }
    }
  Row(
    modifier = modifier.fillMaxWidth().horizontalScroll(scrollState),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    options.forEachIndexed { index, option ->
      val isSelected = selectedIndex == index
      FilterChip(
        selected = isSelected,
        onClick = { onSelect(index) },
        label = { Text(option, maxLines = 1) },
      )
    }
  }
}

/**
 * A section header with consistent styling.
 *
 * @param text The header text
 * @param modifier Modifier to be applied to the header
 */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleLarge,
    color = MaterialTheme.colorScheme.primary,
    modifier = modifier.padding(vertical = 8.dp),
  )
}

/**
 * An empty state placeholder component.
 *
 * @param message The message to display
 * @param actionText Optional action button text
 * @param onAction Optional action button callback
 * @param modifier Modifier to be applied
 */
@Composable
fun EmptyState(
  message: String,
  modifier: Modifier = Modifier,
  actionText: String? = null,
  onAction: (() -> Unit)? = null,
) {
  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = message,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    if (actionText != null && onAction != null) {
      Spacer(Modifier.height(16.dp))
      Button(onClick = onAction) { Text(actionText) }
    }
  }
}

@Composable
fun ButtonRow(title: String, subTitle: String, content: @Composable () -> Unit) {
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
