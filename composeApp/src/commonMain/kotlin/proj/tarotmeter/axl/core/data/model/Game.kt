package proj.tarotmeter.axl.core.data.model

import androidx.compose.runtime.mutableStateListOf
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import proj.tarotmeter.axl.util.DateUtil

/**
 * Game model
 *
 * @property id unique game id, should be autoincrement.
 * @property players list of players in the game.
 * @property rounds list of rounds played in the game, sorted by their index.
 * @property name name for the game.
 * @property startedAt timestamp when the game started.
 * @property updatedAt timestamp when the game was last edited.
 * @property source The storage source of this game (local or remote).
 */
@Serializable
data class Game(
  val players: List<Player>,
  val name: String,
  val id: Uuid = Uuid.random(),
  private val roundsInternal: MutableList<Round> = mutableStateListOf(),
  val startedAt: Instant = DateUtil.now(),
  private var updatedAtInternal: Instant = DateUtil.now(),
  val source: GameSource = GameSource.LOCAL,
) {
  private var isSorted: Boolean = false

  val rounds: List<Round>
    get() {
      if (!isSorted) {
        roundsInternal.sortBy { it.index }
        isSorted = true
      }
      return roundsInternal
    }

  val updatedAt: Instant
    get() = updatedAtInternal

  init {
    require(players.size in 3..5) {
      "The number of players must be between 3 and 5. Was: ${players.size}"
    }
    rounds.forEach { checkRoundConsistent(it) }
  }

  /**
   * Adds a round
   *
   * @param round The round to add.
   */
  fun addRound(round: Round) {
    checkRoundConsistent(round)
    isSorted = isSorted && (roundsInternal.isEmpty() || round.index >= roundsInternal.last().index)
    roundsInternal.add(round)
    updatedAtInternal = round.updatedAt
  }

  private fun checkRoundConsistent(round: Round) {
    require(players.contains(round.taker)) { "Taker must be one of the players in the game" }
    if (players.size == 5) {
      require(round.partner != null) { "A partner is required in a 5-player game" }
      require(players.contains(round.partner)) { "Partner must be one of the players in the game." }
    } else {
      require(round.partner == null) {
        "There should not be any partner if there are less than 5 players"
      }
    }
  }
}
