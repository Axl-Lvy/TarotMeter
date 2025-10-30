package proj.tarotmeter.axl.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
      modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
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
 * @param options List of options to choose from
 * @param selectedIndex Currently selected index
 * @param onSelect Callback when an option is selected
 * @param modifier Modifier to be applied to the button group
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedButtons(
  options: List<String>,
  selectedIndex: Int,
  onSelect: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    options.forEachIndexed { index, option ->
      val isSelected = selectedIndex == index
      FilterChip(
        selected = isSelected,
        onClick = { onSelect(index) },
        label = { Text(option) },
        modifier = Modifier.weight(1f),
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
