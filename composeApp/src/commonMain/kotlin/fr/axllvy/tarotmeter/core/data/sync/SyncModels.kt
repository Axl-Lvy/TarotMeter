package fr.axllvy.tarotmeter.core.data.sync

import fr.axllvy.tarotmeter.core.data.model.Player
import fr.axllvy.tarotmeter.core.data.model.Round
import fr.axllvy.tarotmeter.core.data.model.enums.Chelem
import fr.axllvy.tarotmeter.core.data.model.enums.Contract
import fr.axllvy.tarotmeter.core.data.model.enums.PetitAuBout
import fr.axllvy.tarotmeter.core.data.model.enums.Poignee
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** Lightweight sync row representing a Player (including soft-deleted ones). */
data class PlayerSync(
  val id: Uuid,
  val name: String,
  val updatedAt: Instant,
  val isDeleted: Boolean,
) {
  fun toPlayer() = Player(name, id, updatedAt)
}

/** Lightweight sync row representing a Game (including soft-deleted ones) with its player ids. */
data class GameSync(
  val id: Uuid,
  val name: String,
  val startedAt: Instant,
  val updatedAt: Instant,
  val isDeleted: Boolean,
  val playerIds: List<Uuid>,
)

/** Lightweight sync row representing a Round (including soft-deleted ones). */
data class RoundSync(
  val id: Uuid,
  val gameId: Uuid,
  val takerId: Uuid,
  val partnerId: Uuid?,
  val contract: Contract,
  val oudlerCount: Int,
  val takerPoints: Int,
  val poignee: Poignee,
  val petitAuBout: PetitAuBout,
  val chelem: Chelem,
  val index: Int,
  val updatedAt: Instant,
  val isDeleted: Boolean,
) {
  fun toRound(playerLookup: (Uuid) -> Player) =
    Round(
      taker = playerLookup(takerId),
      partner = partnerId?.let { playerLookup(it) },
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
