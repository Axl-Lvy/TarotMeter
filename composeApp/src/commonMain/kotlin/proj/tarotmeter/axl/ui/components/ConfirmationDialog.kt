package proj.tarotmeter.axl.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.general_cancel
import tarotmeter.composeapp.generated.resources.general_ok

/** A confirmation dialog that can be shown with a confirmation action. */
class ConfirmationDialog(
  private val okText: StringResource = Res.string.general_ok,
  private val cancelText: StringResource = Res.string.general_cancel,
) {
  private var show by mutableStateOf(false)
  private var content by mutableStateOf<@Composable () -> Unit>({})
  private var onConfirm = {}

  /**
   * Show the confirmation dialog with the given confirmation action.
   *
   * @param text The text to show in the dialog.
   * @param confirm The action to perform when the user confirms.
   */
  fun show(text: StringResource, confirm: () -> Unit) {
    show(confirm, content = { Text(stringResource(text)) })
  }

  /**
   * Show the confirmation dialog with the given confirmation action and content.
   *
   * @param confirm The action to perform when the user confirms.
   * @param content The composable content to show in the dialog.
   */
  fun show(confirm: () -> Unit, content: @Composable () -> Unit) {
    onConfirm = confirm
    this.content = content
    show = true
  }

  /**
   * Renders the confirmation dialog if it is currently shown.
   *
   * This composable displays the dialog with the provided content and handles confirmation and
   * cancellation actions. The dialog is only visible when triggered via [show]. On confirmation,
   * the provided action is executed and the dialog is dismissed.
   *
   * **This method should be called as soon as the [ConfirmationDialog] is instantiated**
   */
  @Composable
  fun DrawDialog() {
    if (show) {
      AlertDialog(
        onDismissRequest = {},
        confirmButton = {
          TextButton(
            onClick = {
              onConfirm()
              onConfirm = {}
              show = false
            }
          ) {
            Text(stringResource(okText))
          }
        },
        dismissButton = {
          TextButton(
            onClick = {
              onConfirm = {}
              show = false
            }
          ) {
            Text(stringResource(cancelText))
          }
        },
        title = { content() },
      )
    }
  }
}
