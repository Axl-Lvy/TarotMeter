package fr.tarotmeter.core.data

import androidx.compose.runtime.Immutable
import fr.tarotmeter.core.data.model.Round
import fr.tarotmeter.core.data.model.enums.Chelem
import fr.tarotmeter.core.data.model.enums.Contract
import fr.tarotmeter.core.data.model.enums.PetitAuBout
import fr.tarotmeter.core.data.model.enums.Poignee
import fr.tarotmeter.util.DateUtil
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

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
  val index: Int,
  val id: Uuid = Uuid.random(),
  val updatedAt: Instant = DateUtil.now(),
  val isDeleted: Boolean = false,
) {
  constructor(
    round: Round
  ) : this(
    PlayerLocalStorage(round.taker.name, round.taker.id, round.taker.updatedAt),
    round.partner?.let { partner ->
      PlayerLocalStorage(partner.name, partner.id, partner.updatedAt)
    },
    round.contract,
    round.oudlerCount,
    round.takerPoints,
    round.poignee,
    round.petitAuBout,
    round.chelem,
    round.index,
    round.id,
  )

  fun toRound() =
    Round(
      taker = taker.toPlayer(),
      partner = partner?.toPlayer(),
      contract = contract,
      oudlerCount = oudlerCount,
      takerPoints = takerPoints,
      poignee = poignee,
      petitAuBout = petitAuBout,
      chelem = chelem,
      index = index,
      id = id,
      updatedAt = updatedAt,
    )
}
