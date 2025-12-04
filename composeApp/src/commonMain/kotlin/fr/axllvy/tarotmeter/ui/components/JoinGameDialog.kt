package fr.axllvy.tarotmeter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.axllvy.tarotmeter.core.data.cloud.SharedGamesManager
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.general_cancel
import tarotmeter.composeapp.generated.resources.join_game_dialog_button
import tarotmeter.composeapp.generated.resources.join_game_dialog_cannot_join_own
import tarotmeter.composeapp.generated.resources.join_game_dialog_error
import tarotmeter.composeapp.generated.resources.join_game_dialog_game_not_found
import tarotmeter.composeapp.generated.resources.join_game_dialog_input_label
import tarotmeter.composeapp.generated.resources.join_game_dialog_invalid_code
import tarotmeter.composeapp.generated.resources.join_game_dialog_message
import tarotmeter.composeapp.generated.resources.join_game_dialog_not_authenticated
import tarotmeter.composeapp.generated.resources.join_game_dialog_title

/**
 * Dialog to join a game using an invitation code.
 *
 * @param onDismiss Callback when the dialog is dismissed
 * @param onSuccess Callback when the game is successfully joined
 */
@Composable
fun JoinGameDialog(
  onDismiss: () -> Unit,
  onSuccess: () -> Unit,
  sharedGamesManager: SharedGamesManager = koinInject(),
) {
  var invitationCode by remember { mutableStateOf("") }
  var exception by remember { mutableStateOf<Exception?>(null) }
  var isLoading by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()

  fun doJoinGame(stringCode: String) {
    val code = stringCode.toIntOrNull() ?: return
    isLoading = true
    coroutineScope.launch {
      try {
        sharedGamesManager.joinGame(code)
        onSuccess()
      } catch (e: Exception) {
        exception = e
      } finally {
        isLoading = false
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.join_game_dialog_title)) },
    text = {
      val immutableException = exception
      Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          stringResource(Res.string.join_game_dialog_message),
          style = MaterialTheme.typography.bodyMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = invitationCode,
            onValueChange = {
              if (it.length <= 8 && it.all { char -> char.isDigit() }) {
                invitationCode = it
                exception = null
              }
            },
            label = { Text(stringResource(Res.string.join_game_dialog_input_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = exception != null,
            enabled = !isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          )
          QrCodeScannerButton { doJoinGame(it) }
        }
        QrCodeScannerButton { doJoinGame(it) }
        immutableException?.let {
          Text(
            text = getErrorMessage(immutableException),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = { doJoinGame(invitationCode) },
        enabled = invitationCode.length == 8 && !isLoading,
      ) {
        Text(stringResource(Res.string.join_game_dialog_button))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, enabled = !isLoading) {
        Text(stringResource(Res.string.general_cancel))
      }
    },
  )
}

@Composable
private fun getErrorMessage(e: Exception): String =
  when {
    e.message?.contains("not authenticated", ignoreCase = true) == true ->
      stringResource(Res.string.join_game_dialog_not_authenticated)
    e.message?.contains("invalid or expired invitation code", ignoreCase = true) == true ->
      stringResource(Res.string.join_game_dialog_invalid_code)
    e.message?.contains("game not found", ignoreCase = true) == true ->
      stringResource(Res.string.join_game_dialog_game_not_found)
    e.message?.contains("cannot join your own game", ignoreCase = true) == true ->
      stringResource(Res.string.join_game_dialog_cannot_join_own)
    else -> e.message ?: stringResource(Res.string.join_game_dialog_error)
  }
