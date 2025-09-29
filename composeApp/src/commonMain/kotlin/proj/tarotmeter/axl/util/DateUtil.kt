package proj.tarotmeter.axl.util

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.plus

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
}
