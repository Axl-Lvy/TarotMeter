package proj.tarotmeter.axl.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime

/**
 * Room entity for Game.
 *
 * @property id Unique game identifier.
 * @property startedAt Timestamp when the game was started.
 * @property updatedAt Timestamp when the game was last updated.
 */
@Entity(indices = [Index(value = ["game_id"])])
data class GameEntity(
  @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "game_id") val id: Int,
  @ColumnInfo(name = "started_at") val startedAt: LocalDateTime,
  @ColumnInfo(name = "updated_at") val updatedAt: LocalDateTime,
)
