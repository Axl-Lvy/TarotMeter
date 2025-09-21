package proj.tarotmeter.axl.model

import androidx.compose.runtime.Immutable
import proj.tarotmeter.axl.model.enums.Chelem
import proj.tarotmeter.axl.model.enums.Contract
import proj.tarotmeter.axl.model.enums.PetitAuBout
import proj.tarotmeter.axl.model.enums.Poignee
import proj.tarotmeter.axl.util.IdGenerator

/**
 * Represents a single round of a Tarot game.
 *
 * @property id Unique identifier for the round.
 * @property taker The player who takes the contract.
 * @property partner The partner of the taker, if any.
 * @property contract The contract chosen for the round.
 * @property oudlerCount The number of Oudlers held by the taker (0 to 3).
 * @property takerPoints The number of points scored by the taker (0 to 91).
 * @property poignee The Poignée (handful) announcement for the round.
 * @property petitAuBout The Petit au Bout outcome for the round.
 * @property chelem The Chelem (slam) outcome for the round.
 */
@Immutable
data class Round(
  override val id: Int,
  val taker: Player,
  val partner: Player?,
  val contract: Contract,
  val oudlerCount: Int,
  val takerPoints: Int,
  val poignee: Poignee,
  val petitAuBout: PetitAuBout,
  val chelem: Chelem,
) : AutoIncrement {
  init {
    require(oudlerCount in 0..3) { "Oudler count must be between 0 and 3" }
    require(takerPoints in 0..91) { "Taker points must be between 0 and 91" }
  }

  /**
   * Constructor for creating a [Round] with an auto-generated id.
   *
   * @param taker The player who takes the contract.
   * @param contract The contract chosen for the round.
   * @param partner The partner of the taker, if any.
   * @param oudlerCount The number of Oudlers held by the taker (0 to 3).
   * @param takerPoints The number of points scored by the taker (0 to 91).
   * @param poignee The Poignée (handful) announcement for the round.
   * @param petitAuBout The Petit au Bout outcome for the round.
   * @param chelem The Chelem (slam) outcome for the round.
   */
  constructor(
    taker: Player,
    contract: Contract,
    partner: Player?,
    oudlerCount: Int,
    takerPoints: Int,
    poignee: Poignee,
    petitAuBout: PetitAuBout,
    chelem: Chelem,
  ) : this(
    IdGenerator.nextId(Round::class),
    taker,
    partner,
    contract,
    oudlerCount,
    takerPoints,
    poignee,
    petitAuBout,
    chelem,
  )
}
