package fr.tarotmeter.core.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import fr.tarotmeter.core.data.entity.DeviceIdEntity

/** Data Access Object for miscellaneous operations. */
@Dao
interface MiscDao {

  /** Retrieves the device ID entity from the database. */
  @Query("SELECT * FROM DeviceIdEntity LIMIT 1") suspend fun getDeviceId(): DeviceIdEntity?

  /** Inserts the device ID entity into the database. */
  @Insert(onConflict = IGNORE) suspend fun insertDeviceId(deviceId: DeviceIdEntity)
}
