package proj.tarotmeter.axl.core.data.cloud.model

import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Supabase data model for GameCrossPlayer table.
 *
 * @property playerId ID of the player.
 * @property updatedAt Timestamp when the relationship was last updated.
 * @property gameId ID of the game.
 */
@Serializable
data class SupabaseGameCrossPlayer(
  @SerialName("player_id") val playerId: String,
  @SerialName("updated_at") val updatedAt: Instant,
  @SerialName("game_id") val gameId: String,
  @SerialName("is_deleted") val isDeleted: Boolean = false,
)
