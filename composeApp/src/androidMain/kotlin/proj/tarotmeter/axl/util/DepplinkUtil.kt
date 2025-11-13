package proj.tarotmeter.axl.util

import android.content.Intent
import android.net.Uri

fun parseDeepLink(intent: Intent): String? {
  val data: Uri? = intent.data
  return if (intent.action == Intent.ACTION_VIEW && data != null) {
    parseDeepLink(data)
  } else {
    null
  }
}

private fun parseDeepLink(uri: Uri): String? {
  // Expected format: https://www.axl-lvy.fr/tarotmeter#confirm-email/<token>
  // or https://www.axl-lvy.fr/tarotmeter#join/<code>
  val fragment = uri.fragment
  if (fragment != null) {
    if (fragment.startsWith("confirm-email/")) {
      val token = fragment.removePrefix("confirm-email/")
      if (token.isNotEmpty()) {
        return "confirm-email/$token"
      }
    } else if (fragment.startsWith("join/")) {
      val code = fragment.removePrefix("join/")
      if (code.isNotEmpty() && code.length == 8 && code.all { it.isDigit() }) {
        return "join/$code"
      }
    }
  }
  return null
}
