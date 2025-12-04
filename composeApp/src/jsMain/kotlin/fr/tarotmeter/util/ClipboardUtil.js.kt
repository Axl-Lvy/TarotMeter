@file:OptIn(ExperimentalComposeUiApi::class)

package fr.tarotmeter.util

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.ClipboardItem

actual fun String.toClipEntry() = ClipEntry(createClipboardItemWithPlainText(this))

@Suppress("UNUSED_PARAMETER")
private fun createClipboardItemWithPlainText(text: String): Array<ClipboardItem> =
  js("[new ClipboardItem({'text/plain': new Blob([text], { type: 'text/plain' })})]")
