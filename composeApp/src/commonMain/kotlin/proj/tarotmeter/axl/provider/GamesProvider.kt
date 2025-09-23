package proj.tarotmeter.axl.provider

import kotlin.uuid.Uuid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import proj.tarotmeter.axl.data.DatabaseManager
import proj.tarotmeter.axl.data.model.Game
import proj.tarotmeter.axl.data.model.Round

/** Provides access to and management of games within the application. */
class GamesProvider : KoinComponent {
  private val playersProvider: PlayersProvider by inject()

  private val databaseManager: DatabaseManager by inject()

  /**
   * Creates a new game.
   *
   * @param playerCount The number of players for the new game.
   * @return The created [Game] or null if there are not enough players.
   */
  suspend fun createGame(playerCount: Int): Game? {
    val players = playersProvider.getPlayers()
    if (players.size < playerCount) return null
    val selected = players.take(playerCount)
    val game = Game(players = selected)
    databaseManager.insertGame(game)
    return game
  }

  /**
   * Retrieves a game.
   *
   * @param id The id of the game to retrieve.
   * @return The [Game] with the given id, or null if not found.
   */
  suspend fun getGame(id: Uuid): Game? = databaseManager.getGame(id)

  suspend fun getGames(): List<Game> = databaseManager.getGames()

  /**
   * Adds a [round] to a game.
   *
   * @param gameId The id of the game to add the round to.
   * @param round The [Round] to add.
   */
  suspend fun addRound(gameId: Uuid, round: Round) {
    val game = getGame(gameId) ?: return
    game.addRound(round)
    databaseManager.addRound(gameId, round)
  }
}
