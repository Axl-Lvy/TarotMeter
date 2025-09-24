package proj.tarotmeter.axl.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid
import proj.tarotmeter.axl.core.data.model.Player

/**
 * Room entity for Player.
 *
 * @property id Unique player identifier.
 * @property name Display the name of the player.
 */
@Entity(indices = [Index(value = ["player_id"]), Index(value = ["name"])])
data class PlayerEntity(
  @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "player_id") val id: Uuid,
  val name: String,
) {
  /**
   * Converts this entity to a domain model Player.
   *
   * @return Player domain model.
   */
  fun toPlayer(): Player = Player(name, id)
}
