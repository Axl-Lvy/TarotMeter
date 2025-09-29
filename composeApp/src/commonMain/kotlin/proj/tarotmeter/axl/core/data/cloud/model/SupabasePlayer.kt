package proj.tarotmeter.axl.core.data.cloud.model

import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import proj.tarotmeter.axl.core.data.model.Player

/**
 * Supabase data model for Player table.
 *
 * @property playerId Unique player identifier.
 * @property name Display name of the player.
 * @property updatedAt Timestamp when the player was last updated.
 * @property userId ID of the user who owns this player.
 */
@Serializable
data class SupabasePlayer(
  @SerialName("player_id") val playerId: String,
  val name: String,
  @SerialName("updated_at") val updatedAt: Instant,
  @SerialName("user_id") val userId: String,
  @SerialName("is_deleted") val isDeleted: Boolean = false,
) {
  fun toPlayer() = Player(name, Uuid.parse(playerId), updatedAt)
}
