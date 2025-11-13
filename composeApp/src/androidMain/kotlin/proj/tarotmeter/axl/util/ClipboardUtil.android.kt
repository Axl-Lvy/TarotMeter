package proj.tarotmeter.axl.util

import android.content.ClipData
import androidx.compose.ui.platform.toClipEntry

/** Converts a String to a ClipEntry for clipboard operations on Android. */
actual fun String.toClipEntry() = ClipData.newPlainText(this, this).toClipEntry()
