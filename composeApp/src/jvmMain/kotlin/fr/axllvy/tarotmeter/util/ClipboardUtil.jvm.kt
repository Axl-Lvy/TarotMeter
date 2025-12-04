package fr.axllvy.tarotmeter.util

import androidx.compose.ui.platform.ClipEntry
import java.awt.datatransfer.StringSelection

/** Converts a String to a ClipEntry for clipboard operations on Android. */
actual fun String.toClipEntry() = ClipEntry(StringSelection(this))
