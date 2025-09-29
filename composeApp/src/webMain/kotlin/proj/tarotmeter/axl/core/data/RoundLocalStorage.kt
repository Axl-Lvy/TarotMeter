package proj.tarotmeter.axl.core.data

import androidx.compose.runtime.Immutable
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import proj.tarotmeter.axl.core.data.model.Round
import proj.tarotmeter.axl.core.data.model.enums.Chelem
import proj.tarotmeter.axl.core.data.model.enums.Contract
import proj.tarotmeter.axl.core.data.model.enums.PetitAuBout
import proj.tarotmeter.axl.core.data.model.enums.Poignee
import proj.tarotmeter.axl.util.DateUtil

@Serializable
@Immutable
data class RoundLocalStorage(
  val taker: PlayerLocalStorage,
  val partner: PlayerLocalStorage?,
  val contract: Contract,
  val oudlerCount: Int,
  val takerPoints: Int,
  val poignee: Poignee,
  val petitAuBout: PetitAuBout,
  val chelem: Chelem,
  val id: Uuid = Uuid.random(),
  val updatedAt: Instant = DateUtil.now(),
  val isDeleted: Boolean = false,
) {
  fun toRound() =
    Round(
      id = id,
      updatedAt = updatedAt,
      taker = taker.toPlayer(),
      partner = partner?.toPlayer(),
      contract = contract,
      oudlerCount = oudlerCount,
      takerPoints = takerPoints,
      poignee = poignee,
      petitAuBout = petitAuBout,
      chelem = chelem,
    )
}
