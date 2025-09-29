package proj.tarotmeter.axl.core.data.entity

import androidx.room.Embedded
import androidx.room.Relation
import proj.tarotmeter.axl.core.data.model.Round

/**
 * Room relation data class representing a complete round with associated player entities.
 *
 * @property round The main round entity.
 * @property taker The player who took the contract.
 * @property partner The partner player, if any.
 */
data class RoundWithRefs(
  @Embedded val round: RoundEntity,
  @Relation(parentColumn = "taker_id", entityColumn = "player_id") val taker: PlayerEntity,
  @Relation(parentColumn = "partner_id", entityColumn = "player_id") val partner: PlayerEntity?,
) {
  /**
   * Converts this entity with references to a domain model Round.
   *
   * @return Round domain model with converted player references.
   */
  fun toRound(): Round =
    Round(
      taker.toPlayer(),
      partner?.toPlayer(),
      round.contract,
      round.oudlerCount,
      round.takerPoints,
      round.poignee,
      round.petitAuBout,
      round.chelem,
      round.id,
      round.updatedAt,
    )
}
