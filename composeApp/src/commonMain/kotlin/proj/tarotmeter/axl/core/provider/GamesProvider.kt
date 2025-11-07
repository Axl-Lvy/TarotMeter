package proj.tarotmeter.axl.core.provider

import kotlin.uuid.Uuid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.DatabaseManager
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.core.data.model.Round

/** Provides access to and management of games within the application. */
class GamesProvider : KoinComponent {
  private val databaseManager: DatabaseManager by inject()

  /**
   * Creates a new game.
   *
   * @param players The players involved in this game.
   * @return The created [Game].
   * @throws IllegalArgumentException if the number of players is not between 3 and 5.
   */
  suspend fun createGame(players: Set<Player>): Game {
    require(players.size in 3..5) { "A game must have between 3 and 5 players." }
    val game = Game(players = players.toList())
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

  /**
   * Retrieves all games.
   *
   * @return The list of all [games][Game].
   */
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

  suspend fun deleteRound(roundId: Uuid) {
    databaseManager.deleteRound(roundId)
  }

  suspend fun updateRound(round: Round) {
    databaseManager.updateRound(round)
  }
}
