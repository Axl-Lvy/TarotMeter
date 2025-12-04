package fr.tarotmeter.core.data.cloud.model

import fr.tarotmeter.core.data.sync.GameSync
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Supabase data model for Game table.
 *
 * @property gameId Unique game identifier.
 * @property userId ID of the user who owns this game.
 * @property name Name for the game.
 * @property updatedAt Timestamp when the game was last updated.
 * @property createdAt Timestamp when the game was created.
 */
@Serializable
data class SupabaseGame(
  @SerialName("game_id") val gameId: String,
  @SerialName("user_id") val userId: String,
  @SerialName("name") val name: String,
  @SerialName("updated_at") val updatedAt: Instant,
  @SerialName("created_at") val createdAt: Instant,
  @SerialName("is_deleted") val isDeleted: Boolean,
) {
  fun toGameSync(playerIds: List<Uuid> = emptyList()) =
    GameSync(
      id = Uuid.parse(gameId),
      name = name,
      startedAt = createdAt,
      updatedAt = updatedAt,
      isDeleted = isDeleted,
      playerIds = playerIds,
    )
}
