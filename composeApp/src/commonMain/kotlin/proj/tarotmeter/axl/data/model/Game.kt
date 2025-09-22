package proj.tarotmeter.axl.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.mutableStateListOf
import kotlinx.datetime.LocalDateTime
import proj.tarotmeter.axl.util.DateUtil
import proj.tarotmeter.axl.util.IdGenerator

/**
 * Game model
 *
 * @property id unique game id, should be autoincrement.
 * @property players list of players in the game.
 * @property rounds list of rounds played in the game.
 * @property startedAt timestamp when the game started.
 * @property updatedAt timestamp when the game was last edited.
 */
@Immutable
data class Game(
  override val id: Int,
  val players: List<Player>,
  private val roundsInternal: MutableList<Round> = mutableStateListOf(),
  val startedAt: LocalDateTime = DateUtil.now(),
  private var updatedAtInternal: LocalDateTime = DateUtil.now(),
) : AutoIncrement {
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
   * Default constructor with auto-incremented id
   *
   * @param players list of players in the game.
   */
  constructor(players: List<Player>) : this(id = IdGenerator.nextId(Game::class), players = players)

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
