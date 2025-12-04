package fr.tarotmeter.core.data.cloud.model

import fr.tarotmeter.core.data.model.Player
import fr.tarotmeter.core.data.sync.PlayerSync
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
  @SerialName("is_deleted") val isDeleted: Boolean,
) {
  fun toPlayer() = Player(name, Uuid.parse(playerId), updatedAt)

  fun toPlayerSync() =
    PlayerSync(id = Uuid.parse(playerId), name = name, updatedAt = updatedAt, isDeleted = isDeleted)
}
