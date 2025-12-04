package fr.axllvy.tarotmeter.util

import kotlinx.browser.window

fun getInitialRouteFromUrl(): String? {
  val hash = window.location.hash
  return if (hash.isNotEmpty() && hash.startsWith("#")) {
    hash.substring(1) // Remove the '#' prefix
  } else {
    null
  }
}
