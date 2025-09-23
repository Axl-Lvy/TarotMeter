package proj.tarotmeter.axl.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import proj.tarotmeter.axl.util.DateUtil

/**
 * Game model
 *
 * @property id unique game id, should be autoincrement.
 * @property players list of players in the game.
 * @property rounds list of rounds played in the game.
 * @property startedAt timestamp when the game started.
 * @property updatedAt timestamp when the game was last edited.
 */
@Serializable
@Immutable
data class Game(
  val players: List<Player>,
  val id: Uuid = Uuid.random(),
  private val roundsInternal: MutableList<Round> = mutableStateListOf(),
  val startedAt: LocalDateTime = DateUtil.now(),
  private var updatedAtInternal: LocalDateTime = DateUtil.now(),
) {
  val rounds: List<Round>
    get() = roundsInternal

  val updatedAt: LocalDateTime
    get() = updatedAtInternal

  init {
    require(players.size in 3..5) {
      "Number of players must be between 3 and 5. Was: ${players.size}"
    }
  }

  /**
   * Adds a round
   *
   * @param round The round to add.
   */
  fun addRound(round: Round) {
    roundsInternal.add(round)
    updatedAtInternal = DateUtil.now()
  }
}
