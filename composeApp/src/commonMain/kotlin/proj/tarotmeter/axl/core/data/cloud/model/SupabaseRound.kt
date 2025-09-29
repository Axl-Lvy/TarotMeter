package proj.tarotmeter.axl.core.data.cloud.model

import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.core.data.model.Round
import proj.tarotmeter.axl.core.data.model.enums.Chelem
import proj.tarotmeter.axl.core.data.model.enums.Contract
import proj.tarotmeter.axl.core.data.model.enums.PetitAuBout
import proj.tarotmeter.axl.core.data.model.enums.Poignee

/**
 * Supabase data model for Round table.
 *
 * @property roundId Unique round identifier.
 * @property updatedAt Timestamp when the round was last updated.
 * @property taker ID of the taker player.
 * @property partner ID of the partner player (nullable).
 * @property contract Contract type.
 * @property oudlerCount Number of Oudlers (0-3).
 * @property takerPoints Points scored by taker (0-91).
 * @property poignee Poignee type.
 * @property petitAuBout Petit au Bout outcome.
 * @property chelem Chelem outcome.
 * @property gameId Foreign key to Game.
 */
@Serializable
data class SupabaseRound(
  @SerialName("round_id") val roundId: String,
  @SerialName("updated_at") val updatedAt: Instant,
  val taker: String,
  val partner: String?,
  val contract: Contract,
  @SerialName("oudler_count") val oudlerCount: Int,
  @SerialName("taker_points") val takerPoints: Int,
  val poignee: Poignee,
  @SerialName("petit_au_bout") val petitAuBout: PetitAuBout,
  val chelem: Chelem,
  @SerialName("game_id") val gameId: String,
  @SerialName("is_deleted") val isDeleted: Boolean = false,
) {
  fun toRound(playerProvider: (String) -> Player) =
    Round(
      id = Uuid.parse(roundId),
      updatedAt = updatedAt,
      taker = playerProvider(taker),
      partner = partner?.let { playerProvider(partner) },
      contract = contract,
      oudlerCount = oudlerCount,
      takerPoints = takerPoints,
      poignee = poignee,
      petitAuBout = petitAuBout,
      chelem = chelem,
    )
}
