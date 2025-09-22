package proj.tarotmeter.axl.provider

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import proj.tarotmeter.axl.model.Game
import proj.tarotmeter.axl.model.Round

/**
 * Provides access to and management of games within the application.
 *
 * @property games The list of all games, sorted by their last update time.
 */
class GamesProvider : KoinComponent {
  private val playersProvider: PlayersProvider by inject()

  private val gamesPerId = mutableMapOf<Int, Game>()

  val games: List<Game>
    get() = gamesPerId.values.sortedBy { it.updatedAt }

  /**
   * Creates a new game.
   *
   * @param playerCount The number of players for the new game.
   * @return The created [Game] or null if there are not enough players.
   */
  fun createGame(playerCount: Int): Game? {
    val players = playersProvider.players
    if (players.size < playerCount) return null
    val selected = players.take(playerCount)
    val game = Game(players = selected.toList())
    gamesPerId[game.id] = game
    return game
  }

  /**
   * Retrieves a game.
   *
   * @param id The id of the game to retrieve.
   * @return The [Game] with the given id, or null if not found.
   */
  fun getGame(id: Int): Game? = gamesPerId[id]

  /**
   * Adds a [round] to a game.
   *
   * @param gameId The id of the game to add the round to.
   * @param round The [Round] to add.
   */
  fun addRound(gameId: Int, round: Round) {
    val game = getGame(gameId) ?: return
    game.addRound(round)
  }
}
