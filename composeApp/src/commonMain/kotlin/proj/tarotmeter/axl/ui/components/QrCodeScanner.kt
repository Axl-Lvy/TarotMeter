package proj.tarotmeter.axl.ui.components

import androidx.compose.runtime.Composable

@Composable expect fun QrCodeScannerButton(onInvitationCodeFound: (String) -> Unit)
