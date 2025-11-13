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
import androidx.compose.foundation.layout.padding
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
import io.github.goquati.qr.QrCode
import kotlin.uuid.Uuid
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import proj.tarotmeter.axl.core.data.cloud.SharedGamesManager
import proj.tarotmeter.axl.util.toClipEntry
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.general_ok
import tarotmeter.composeapp.generated.resources.invitation_code_dialog_copy
import tarotmeter.composeapp.generated.resources.invitation_code_dialog_error
import tarotmeter.composeapp.generated.resources.invitation_code_dialog_generating
import tarotmeter.composeapp.generated.resources.invitation_code_dialog_title

/**
 * Dialog to display an invitation code for a game, with QR code and copy button.
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
        val code = sharedGamesManager.createGameInvitation(gameId)
        invitationCode = code
        qrCode = generateQrCode(code.toString().padStart(8, '0'))
        isLoading = false
      } catch (e: Exception) {
        errorMessage = e.message ?: "Unknown error"
        isLoading = false
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(Res.string.invitation_code_dialog_title)) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        val (immutableErrorMessage, immutableInvitationCode, immutableQrCode) =
          Triple(errorMessage, invitationCode, qrCode)
        when {
          isLoading -> {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              stringResource(Res.string.invitation_code_dialog_generating),
              style = MaterialTheme.typography.bodyMedium,
            )
          }
          immutableErrorMessage != null -> {
            Text(
              stringResource(Res.string.invitation_code_dialog_error, immutableErrorMessage),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.error,
            )
          }
          immutableInvitationCode != null && immutableQrCode != null -> {
            GameInvitationDialogContent(immutableQrCode, qrCode, immutableInvitationCode)
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
private fun GameInvitationDialogContent(
  immutableQrCode: QrCode,
  qrCode: QrCode?,
  immutableInvitationCode: Int,
) {
  // QR code centered
  Box(modifier = Modifier.aspectRatio(1f).fillMaxWidth(0.7f), contentAlignment = Alignment.Center) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      for (rowIndex in 0..<immutableQrCode.size) {
        Row(modifier = Modifier.weight(1f)) {
          for (colIndex in 0..<qrCode!!.size) {
            Box(
              modifier =
                Modifier.fillMaxSize()
                  .weight(1f)
                  .background(if (immutableQrCode[rowIndex, colIndex]) Color.Black else Color.White)
            )
          }
        }
      }
    }
  }
  Spacer(modifier = Modifier.height(16.dp))
  // Code and copy button centered
  val clipBoardManager = LocalClipboard.current
  val stringCode = immutableInvitationCode.toString().padStart(8, '0')
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    SelectionContainer {
      Text(
        text = stringCode.replaceRange(4, 4, " "),
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(end = 8.dp),
      )
    }
    val coroutineScope = rememberCoroutineScope()
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

private fun generateQrCode(stringCode: String) =
  QrCode.encodeText("https://www.axl-lvy.fr/tarotmeter#join/$stringCode", QrCode.Ecc.LOW)
