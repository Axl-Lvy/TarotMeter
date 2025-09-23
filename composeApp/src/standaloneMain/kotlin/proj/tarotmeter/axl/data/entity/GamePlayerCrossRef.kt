package proj.tarotmeter.axl.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlin.uuid.Uuid

/**
 * Cross-reference entity for the many-to-many relationship between Game and Player.
 *
 * @property gameId Foreign key to GameEntity.
 * @property playerId Foreign key to PlayerEntity.
 */
@Entity(
  primaryKeys = ["game_id", "player_id"],
  foreignKeys =
    [
      ForeignKey(
        entity = GameEntity::class,
        parentColumns = ["game_id"],
        childColumns = ["game_id"],
        onDelete = ForeignKey.CASCADE,
      ),
      ForeignKey(
        entity = PlayerEntity::class,
        parentColumns = ["player_id"],
        childColumns = ["player_id"],
        onDelete = ForeignKey.CASCADE,
      ),
    ],
  indices = [Index(value = ["game_id"]), Index(value = ["player_id"])],
)
data class GamePlayerCrossRef(
  @ColumnInfo(name = "game_id") val gameId: Uuid,
  @ColumnInfo(name = "player_id") val playerId: Uuid,
)
