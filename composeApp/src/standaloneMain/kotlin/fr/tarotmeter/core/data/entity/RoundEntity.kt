package fr.tarotmeter.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import fr.tarotmeter.core.data.model.enums.Chelem
import fr.tarotmeter.core.data.model.enums.Contract
import fr.tarotmeter.core.data.model.enums.PetitAuBout
import fr.tarotmeter.core.data.model.enums.Poignee
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Room entity for Round.
 *
 * @property id Unique round identifier.
 * @property gameId Foreign key to GameEntity.
 * @property takerId Foreign key to PlayerEntity (taker).
 * @property partnerId Foreign key to PlayerEntity (partner, nullable).
 * @property contract Contract type.
 * @property oudlerCount Number of Oudlers (0-3).
 * @property takerPoints Points scored by taker (0-91).
 * @property poignee Poignee type.
 * @property petitAuBout Petit au Bout outcome.
 * @property chelem Chelem outcome.
 * @property index The index of the round in the game round list.
 */
@Entity(
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
        childColumns = ["taker_id"],
        onDelete = ForeignKey.CASCADE,
      ),
      ForeignKey(
        entity = PlayerEntity::class,
        parentColumns = ["player_id"],
        childColumns = ["partner_id"],
        onDelete = ForeignKey.CASCADE,
      ),
    ],
  indices =
    [
      Index(value = ["round_id"]),
      Index(value = ["game_id"]),
      Index(value = ["taker_id"]),
      Index(value = ["partner_id"]),
    ],
)
data class RoundEntity(
  @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "round_id") val id: Uuid,
  @ColumnInfo(name = "game_id") val gameId: Uuid,
  @ColumnInfo(name = "taker_id") val takerId: Uuid,
  @ColumnInfo(name = "partner_id") val partnerId: Uuid?,
  val contract: Contract,
  @ColumnInfo(name = "oudler_count") val oudlerCount: Int,
  @ColumnInfo(name = "taker_points") val takerPoints: Int,
  val poignee: Poignee,
  @ColumnInfo(name = "petit_au_bout") val petitAuBout: PetitAuBout,
  val index: Int,
  val chelem: Chelem,
  @ColumnInfo(name = "updated_at") val updatedAt: Instant,
  @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
)
