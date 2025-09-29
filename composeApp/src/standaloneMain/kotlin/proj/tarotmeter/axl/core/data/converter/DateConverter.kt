package proj.tarotmeter.axl.core.data.converter

import androidx.room.TypeConverter
import kotlin.time.Instant

/** Type converters to handle kotlin.time.Instant type. */
class DateConverter {

  /** Converts an Instant to a Long (milliseconds since epoch) for storage in the database. */
  @TypeConverter
  fun fromLocalTime(date: Instant?): Long? {
    return date?.toEpochMilliseconds()
  }

  /** Converts a Long (epoch milliseconds) from the database back to an Instant. */
  @TypeConverter
  fun toLocalTime(dateLong: Long?): Instant? {
    if (dateLong == null) return null
    return Instant.fromEpochMilliseconds(dateLong)
  }
}
