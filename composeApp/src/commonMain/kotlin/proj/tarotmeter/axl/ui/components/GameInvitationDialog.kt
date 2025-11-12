package proj.tarotmeter.axl.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Copy
import compose.icons.fontawesomeicons.solid.Qrcode
import io.github.goquati.qr.QrCode
import kotlin.uuid.Uuid
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import proj.tarotmeter.axl.core.data.cloud.SharedGamesManager
import proj.tarotmeter.axl.util.toClipEntry
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.general_cancel
import tarotmeter.composeapp.generated.resources.general_ok
import tarotmeter.composeapp.generated.resources.invitation_code_dialog_copy
import tarotmeter.composeapp.generated.resources.invitation_code_dialog_error
import tarotmeter.composeapp.generated.resources.invitation_code_dialog_generating
import tarotmeter.composeapp.generated.resources.invitation_code_dialog_message
import tarotmeter.composeapp.generated.resources.invitation_code_dialog_qr
import tarotmeter.composeapp.generated.resources.invitation_code_dialog_title

/**
 * Dialog to display an invitation code for a game.
 *
 * @param gameId The ID of the game to create an invitation for
 * @param onDismiss Callback when the dialog is dismissed
 */
@Composable
fun GameInvitationDialog(
  gameId: Uuid,
  onDismiss: () -> Unit,
  sharedGamesManager: SharedGamesManager = koinInject(),
) {
  var invitationCode by remember { mutableStateOf<Int?>(null) }
  var qrCode by remember { mutableStateOf<QrCode?>(null) }
  var isLoading by remember { mutableStateOf(true) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  val coroutineScope = rememberCoroutineScope()

  LaunchedEffect(gameId) {
    coroutineScope.launch {
      try {
        invitationCode = sharedGamesManager.createGameInvitation(gameId)
        isLoading = false
      } catch (e: Exception) {
        errorMessage = e.message ?: "Unknown error"
        isLoading = false
      }
    }
  }

  val immutableQrCode = qrCode
  if (immutableQrCode == null) {
    NumericInvitationDialog(onDismiss, isLoading, errorMessage, invitationCode, { qrCode = it })
  } else {
    QrCodeInvitationDialog(immutableQrCode, onDismiss = { qrCode = null })
  }
}

@Composable
private fun NumericInvitationDialog(
  onDismiss: () -> Unit,
  isLoading: Boolean,
  errorMessage: String?,
  invitationCode: Int?,
  onGenerateQrCode: (QrCode) -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.invitation_code_dialog_title)) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        when {
          isLoading -> {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              stringResource(Res.string.invitation_code_dialog_generating),
              style = MaterialTheme.typography.bodyMedium,
            )
          }

          errorMessage != null -> {
            Text(
              stringResource(Res.string.invitation_code_dialog_error, errorMessage!!),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.error,
            )
          }

          invitationCode != null -> {
            Text(
              stringResource(Res.string.invitation_code_dialog_message),
              style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              val clipBoardManager = LocalClipboard.current
              val coroutineScope = rememberCoroutineScope()
              val stringCode = invitationCode.toString().padStart(8, '0')
              TextButton(onClick = { onGenerateQrCode(generateQrCode(stringCode)) }) {
                Icon(
                  imageVector = FontAwesomeIcons.Solid.Qrcode,
                  contentDescription = stringResource(Res.string.invitation_code_dialog_qr),
                  modifier = Modifier.size(20.dp),
                )
              }
              SelectionContainer {
                Text(
                  text = stringCode.replaceRange(4, 4, " "),
                  style = MaterialTheme.typography.displaySmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                )
              }
              TextButton(
                onClick = {
                  coroutineScope.launch { clipBoardManager.setClipEntry(stringCode.toClipEntry()) }
                }
              ) {
                Icon(
                  imageVector = FontAwesomeIcons.Solid.Copy,
                  contentDescription = stringResource(Res.string.invitation_code_dialog_copy),
                  modifier = Modifier.size(20.dp),
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(Res.string.general_ok)) }
    },
  )
}

@Composable
private fun QrCodeInvitationDialog(qrCode: QrCode, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    text = {
      Box(modifier = Modifier.aspectRatio(1f)) {
        Column(verticalArrangement = Arrangement.Center) {
          for (rowIndex in 0..<qrCode.size) {
            Row(modifier = Modifier.weight(1f)) {
              for (colIndex in 0..<qrCode.size) {
                Box(
                  modifier =
                    Modifier.fillMaxSize()
                      .weight(1f)
                      .background(if (qrCode[rowIndex, colIndex]) Color.Black else Color.White)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(Res.string.general_cancel)) }
    },
  )
}

private fun generateQrCode(stringCode: String) =
  QrCode.encodeText("https://www.axl-lvy.fr/tarotmeter#join/$stringCode", QrCode.Ecc.LOW)
