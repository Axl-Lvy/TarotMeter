package fr.axllvy.tarotmeter.core.data.model

import androidx.compose.runtime.Immutable
import fr.axllvy.tarotmeter.core.data.model.enums.Chelem
import fr.axllvy.tarotmeter.core.data.model.enums.Contract
import fr.axllvy.tarotmeter.core.data.model.enums.PetitAuBout
import fr.axllvy.tarotmeter.core.data.model.enums.Poignee
import fr.axllvy.tarotmeter.util.DateUtil
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

/**
 * Represents a single round of a Tarot game.
 *
 * @property id Unique identifier for the round.
 * @property index The index of the round in the game round list.
 * @property taker The player who takes the contract.
 * @property partner The partner of the taker, if any.
 * @property contract The contract chosen for the round.
 * @property oudlerCount The number of Oudlers held by the taker (0 to 3).
 * @property takerPoints The number of points scored by the taker (0 to 91).
 * @property poignee The Poignée (handful) announcement for the round.
 * @property petitAuBout The Petit au Bout outcome for the round.
 * @property chelem The Chelem (slam) outcome for the round.
 */
@Serializable
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
  val index: Int,
  val id: Uuid = Uuid.random(),
  val updatedAt: Instant = DateUtil.now(),
) {
  init {
    require(oudlerCount in 0..3) { "Oudler count must be between 0 and 3" }
    require(takerPoints in 0..91) { "Taker points must be between 0 and 91" }
  }
}
