package fr.axllvy.tarotmeter.ui.components

import androidx.compose.runtime.Composable

@Composable expect fun QrCodeScannerButton(onInvitationCodeFound: (String) -> Unit)
