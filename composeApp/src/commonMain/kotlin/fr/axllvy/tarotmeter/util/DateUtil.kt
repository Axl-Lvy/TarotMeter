package fr.axllvy.tarotmeter.util

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime

/**
 * Utility object providing common date and time operations using kotlinx-datetime.
 *
 * This object centralizes date/time functionality for the application, providing convenient methods
 * for getting current time, calculating date differences, and performing date arithmetic
 * operations.
 */
object DateUtil {

  /**
   * Returns the current date and time in the system's default timezone.
   *
   * @return Current [LocalDateTime] in system timezone
   */
  fun now(): Instant {
    return Clock.System.now()
  }

  fun referenceTimePast(): Instant {
    return Instant.parse("2000-01-01T00:00:00Z")
  }
}
