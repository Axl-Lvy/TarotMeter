package proj.tarotmeter.axl.data.converter

import androidx.room.TypeConverter
import kotlin.uuid.Uuid

/** Uuid converter */
class UuidConverter {
  /** Converts an Uuid to a String for storage in the database. */
  @TypeConverter
  fun fromUuId(uuid: Uuid?): String? {
    return uuid?.toHexString()
  }

  /** Converts a String from the database back to an Uuid. */
  @TypeConverter
  fun toUuId(stringId: String?): Uuid? {
    return stringId?.let { Uuid.parseHex(stringId) }
  }
}
