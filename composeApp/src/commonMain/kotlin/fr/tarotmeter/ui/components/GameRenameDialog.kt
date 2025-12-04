package fr.tarotmeter.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.general_cancel
import tarotmeter.composeapp.generated.resources.general_save
import tarotmeter.composeapp.generated.resources.history_rename_game
import tarotmeter.composeapp.generated.resources.history_rename_game_label
import tarotmeter.composeapp.generated.resources.history_rename_game_placeholder

/**
 * Dialog for renaming a game.
 *
 * @param currentName The current name of the game.
 * @param onDismiss Callback when the dialog is dismissed.
 * @param onConfirm Callback when the new name is confirmed.
 */
@Composable
fun GameRenameDialog(
  currentName: String,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var gameName by remember { mutableStateOf(currentName) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.history_rename_game)) },
    text = {
      OutlinedTextField(
        value = gameName,
        onValueChange = { gameName = it },
        label = { Text(stringResource(Res.string.history_rename_game_label)) },
        placeholder = { Text(stringResource(Res.string.history_rename_game_placeholder)) },
        singleLine = true,
      )
    },
    confirmButton = {
      TextButton(
        onClick = {
          if (gameName.trim().isNotBlank()) {
            onConfirm(gameName.trim())
          }
        },
        enabled = gameName.trim().isNotBlank(),
      ) {
        Text(stringResource(Res.string.general_save))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(Res.string.general_cancel)) }
    },
    modifier = modifier,
  )
}
