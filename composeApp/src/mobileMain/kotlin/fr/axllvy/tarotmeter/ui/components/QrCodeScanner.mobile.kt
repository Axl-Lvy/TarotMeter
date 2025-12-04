package fr.axllvy.tarotmeter.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Camera
import io.github.ismoy.belzspeedscan.BelZSpeedScanner
import org.jetbrains.compose.resources.stringResource
import tarotmeter.composeapp.generated.resources.Res
import tarotmeter.composeapp.generated.resources.invitation_code_dialog_copy

@Composable
actual fun QrCodeScannerButton(onInvitationCodeFound: (String) -> Unit) {
  var showScanner by remember { mutableStateOf(false) }
  if (showScanner) {
    BelZSpeedScanner(
      onCodeScanned = { code -> code.toInvitationCodeOrNull()?.let { onInvitationCodeFound(it) } }
    )
  }

  TextButton(onClick = { showScanner = true }, modifier = Modifier.fillMaxWidth()) {
    Icon(
      imageVector = FontAwesomeIcons.Solid.Camera,
      contentDescription = stringResource(Res.string.invitation_code_dialog_copy),
      modifier = Modifier.size(20.dp),
    )
  }
}

private fun String.toInvitationCodeOrNull(): String? {
  return if (this.startsWith("https://www.axl-lvy.fr/tarotmeter#join/")) {
    this.removePrefix("https://www.axl-lvy.fr/tarotmeter#join/")
  } else {
    null
  }
}
