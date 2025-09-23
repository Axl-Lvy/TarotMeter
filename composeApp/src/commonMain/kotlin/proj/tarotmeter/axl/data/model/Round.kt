package proj.tarotmeter.axl.data.model

import androidx.compose.runtime.Immutable
import kotlin.uuid.Uuid
import proj.tarotmeter.axl.data.model.enums.Chelem
import proj.tarotmeter.axl.data.model.enums.Contract
import proj.tarotmeter.axl.data.model.enums.PetitAuBout
import proj.tarotmeter.axl.data.model.enums.Poignee

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
  val taker: Player,
  val partner: Player?,
  val contract: Contract,
  val oudlerCount: Int,
  val takerPoints: Int,
  val poignee: Poignee,
  val petitAuBout: PetitAuBout,
  val chelem: Chelem,
  val id: Uuid = Uuid.random(),
) {
  init {
    require(oudlerCount in 0..3) { "Oudler count must be between 0 and 3" }
    require(takerPoints in 0..91) { "Taker points must be between 0 and 91" }
  }
}
