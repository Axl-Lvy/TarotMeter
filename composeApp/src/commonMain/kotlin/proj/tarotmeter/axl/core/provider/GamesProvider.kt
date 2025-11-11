package proj.tarotmeter.axl.core.provider

import kotlin.uuid.Uuid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.DatabaseManager
import proj.tarotmeter.axl.core.data.cloud.SharedGamesManager
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.GameSource
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.core.data.model.Round

/** Provides access to and management of games within the application. */
class GamesProvider : KoinComponent {
  private val databaseManager: DatabaseManager by inject()
  private val sharedGamesManager: SharedGamesManager by inject()

  /**
   * Retrieves a game.
   *
   * @param id The id of the game to retrieve.
   * @return The [Game] with the given id, or null if not found.
   */
  suspend fun getGame(id: Uuid): Game? {
    // Try local first
    val localGame = databaseManager.getGame(id)
    if (localGame != null) {
      return localGame
    }

    // Try remote if not found locally
    val remoteGames = sharedGamesManager.getNonOwnedGames()
    return remoteGames.find { it.id == id }
  }

  /**
   * Retrieves all games (both local and remote).
   *
   * @return The list of all [games][Game].
   */
  suspend fun getGames(): List<Game> {
    val localGames = databaseManager.getGames()
    val remoteGames = sharedGamesManager.getNonOwnedGames()
    return localGames + remoteGames
  }

  /**
   * Creates a new game.
   *
   * @param players The set of players in the game.
   * @param name Name for the game.
   * @return The created [Game].
   */
  suspend fun createGame(players: Set<Player>, name: String): Game {
    val game = Game(players = players.toList(), name = name)
    databaseManager.insertGame(game)
    return game
  }

  /**
   * Renames an existing game.
   *
   * @param id The id of the game to rename.
   * @param newName The new name for the game.
   */
  suspend fun renameGame(id: Uuid, newName: String) {
    val game = getGame(id) ?: return
    when (game.source) {
      GameSource.LOCAL -> databaseManager.renameGame(id, newName)
      GameSource.REMOTE -> {
        // Remote games cannot be renamed directly
        throw UnsupportedOperationException("Cannot rename remote games")
      }
    }
  }

  /**
   * Adds a [round] to a game.
   *
   * @param gameId The id of the game to add the round to.
   * @param round The [Round] to add.
   */
  suspend fun addRound(gameId: Uuid, round: Round) {
    val game = getGame(gameId) ?: return
    game.addRound(round)

    when (game.source) {
      GameSource.LOCAL -> databaseManager.addRound(gameId, round)
      GameSource.REMOTE -> sharedGamesManager.upsertRound(gameId, round)
    }
  }

  suspend fun deleteRound(roundId: Uuid) {
    // First, find which game this round belongs to
    val allGames = getGames()
    val game = allGames.find { game -> game.rounds.any { it.id == roundId } }

    if (game != null) {
      when (game.source) {
        GameSource.LOCAL -> databaseManager.deleteRound(roundId)
        GameSource.REMOTE -> sharedGamesManager.deleteRound(roundId)
      }
    }
  }

  suspend fun updateRound(round: Round) {
    // First, find which game this round belongs to
    val allGames = getGames()
    val game = allGames.find { game -> game.rounds.any { it.id == round.id } }

    if (game != null) {
      when (game.source) {
        GameSource.LOCAL -> databaseManager.updateRound(round)
        GameSource.REMOTE -> sharedGamesManager.upsertRound(game.id, round)
      }
    }
  }
}
