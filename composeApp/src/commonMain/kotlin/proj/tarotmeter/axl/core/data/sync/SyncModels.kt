package proj.tarotmeter.axl.core.data.sync

import kotlin.time.Instant
import kotlin.uuid.Uuid
import proj.tarotmeter.axl.core.data.model.enums.Chelem
import proj.tarotmeter.axl.core.data.model.enums.Contract
import proj.tarotmeter.axl.core.data.model.enums.PetitAuBout
import proj.tarotmeter.axl.core.data.model.enums.Poignee

/** Lightweight sync row representing a Player (including soft-deleted ones). */
data class PlayerSync(
  val id: Uuid,
  val name: String,
  val updatedAt: Instant,
  val isDeleted: Boolean,
)

/** Lightweight sync row representing a Game (including soft-deleted ones) with its player ids. */
data class GameSync(
  val id: Uuid,
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
)
