package fr.axllvy.tarotmeter.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import fr.axllvy.tarotmeter.core.data.model.Player
import kotlin.time.Instant
import kotlin.uuid.Uuid

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
  @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
) {
  /**
   * Converts this entity to a domain model Player.
   *
   * @return Player domain model.
   */
  fun toPlayer(): Player = Player(name, id, updatedAt)
}
