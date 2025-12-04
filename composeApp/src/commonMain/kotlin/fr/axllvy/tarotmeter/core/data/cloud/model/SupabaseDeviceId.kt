package fr.axllvy.tarotmeter.core.data.cloud.model

import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Supabase data model for DeviceId table.
 *
 * @property deviceId The unique identifier for the device.
 * @property userId ID of the user associated with this device.
 * @property updatedAt Timestamp when the device ID was last updated.
 */
@Serializable
data class SupabaseDeviceId(
  @SerialName("device_id") val deviceId: String,
  @SerialName("user_id") val userId: String,
  @SerialName("updated_at") val updatedAt: Instant,
)
