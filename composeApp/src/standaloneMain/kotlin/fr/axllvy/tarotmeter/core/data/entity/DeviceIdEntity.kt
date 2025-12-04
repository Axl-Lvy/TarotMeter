package fr.axllvy.tarotmeter.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

/**
 * Entity representing the device ID.
 *
 * @property deviceId The unique identifier for the device.
 */
@Entity
data class DeviceIdEntity(
  @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "device_id") val deviceId: Uuid
)
