package proj.tarotmeter.axl.data.converter

import androidx.room.TypeConverter
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

private const val numberOfSecondsInOneDay = 3600 * 24

/** Type converters for to handle kotlinx.datetime.LocalDate type. */
@OptIn(ExperimentalTime::class)
class DateConverter {
  /** Converts a LocalDate to a String for storage in the database. */
  @TypeConverter
  fun fromLocalDate(date: LocalDate?): Long? {
    return date?.toEpochDays()
  }

  /** Converts a String from the database back to a LocalDate. */
  @TypeConverter
  fun toLocalDate(epochDays: Long?): LocalDate? {
    return if (epochDays == null) null else LocalDate.fromEpochDays(epochDays)
  }

  /** Converts a LocalDateTime to a String for storage in the database. */
  @TypeConverter
  fun fromLocalTime(date: LocalDateTime?): Long? {
    return date?.toInstant(TimeZone.UTC)?.epochSeconds
  }

  /** Converts a String from the database back to a LocalDateTime. */
  @TypeConverter
  fun toLocalTime(dateLong: Long?): LocalDateTime? {
    if (dateLong == null) return null
    return LocalDateTime(
      LocalDate.fromEpochDays((dateLong / numberOfSecondsInOneDay).toInt()),
      LocalTime.fromSecondOfDay((dateLong % numberOfSecondsInOneDay).toInt()),
    )
  }
}
