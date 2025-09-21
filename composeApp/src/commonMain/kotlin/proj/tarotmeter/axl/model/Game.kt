package proj.tarotmeter.axl.model

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
 * @property startedAtMillis timestamp when the game started.
 */
@Immutable
data class Game(
  override val id: Int,
  val players: List<Player>,
  val rounds: MutableList<Round> = mutableStateListOf(),
  val startedAtMillis: LocalDateTime = DateUtil.now(),
) : AutoIncrement {
  init {
    require(players.size in 3..5) { "Number of players must be between 3 and 5" }
  }

  /**
   * Default constructor with auto-incremented id
   *
   * @param players list of players in the game.
   */
  constructor(players: List<Player>) : this(id = IdGenerator.nextId(Game::class), players = players)
}
