package proj.tarotmeter.axl.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Instant
import kotlin.uuid.Uuid
import proj.tarotmeter.axl.core.data.model.Player

/**
 * Room entity for Player.
 *
 * @property id Unique player identifier.
 * @property name Display the name of the player.
 * @property updatedAt Timestamp when the player was last updated.
 */
@Entity(indices = [Index(value = ["player_id"]), Index(value = ["name"])])
data class PlayerEntity(
  @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "player_id") val id: Uuid,
  val name: String,
  @ColumnInfo(name = "updated_at") val updatedAt: Instant,
) {
  /**
   * Converts this entity to a domain model Player.
   *
   * @return Player domain model.
   */
  fun toPlayer(): Player = Player(name, id, updatedAt)
}
