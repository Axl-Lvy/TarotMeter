package fr.axllvy.tarotmeter.core.data

import fr.axllvy.tarotmeter.core.data.model.Game
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
data class GameLocalStorage(
  val players: List<PlayerLocalStorage>,
  val id: Uuid,
  val roundsInternal: MutableList<RoundLocalStorage>,
  val name: String,
  val startedAt: Instant,
  var updatedAtInternal: Instant,
  val isDeleted: Boolean = false,
) {
  fun toGame(): Game {
    return Game(
      players = players.map { it.toPlayer() },
      name = name,
      id = id,
      roundsInternal = roundsInternal.map { it.toRound() }.toMutableList(),
      startedAt = startedAt,
      updatedAtInternal = updatedAtInternal,
    )
  }
}
