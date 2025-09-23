package proj.tarotmeter.axl.data

import kotlin.uuid.Uuid
import proj.tarotmeter.axl.data.model.Game
import proj.tarotmeter.axl.data.model.Player
import proj.tarotmeter.axl.data.model.Round

/** Interface for managing database operations. */
interface DatabaseManager {

  /**
   * Retrieves all players from the database.
   *
   * @return List of all players.
   */
  suspend fun getPlayers(): List<Player>

  /**
   * Inserts a new player into the database.
   *
   * @param player The player to insert.
   */
  suspend fun insertPlayer(player: Player)

  /**
   * Renames an existing player.
   *
   * @param id The player's ID.
   * @param newName The new name for the player.
   */
  suspend fun renamePlayer(id: Uuid, newName: String)

  /**
   * Deletes a player from the database.
   *
   * @param id The player's ID to delete.
   */
  suspend fun deletePlayer(id: Uuid)

  /**
   * Retrieves all games from the database.
   *
   * @return List of all games.
   */
  suspend fun getGames(): List<Game>

  /**
   * Retrieves a specific game by ID.
   *
   * @param id The game ID.
   * @return The game if found, null otherwise.
   */
  suspend fun getGame(id: Uuid): Game?

  /**
   * Inserts a new game into the database.
   *
   * @param game The game to insert.
   */
  suspend fun insertGame(game: Game)

  /**
   * Adds a round to an existing game.
   *
   * @param gameId The game ID.
   * @param round The round to add.
   */
  suspend fun addRound(gameId: Uuid, round: Round)

  /**
   * Removes a game from the database.
   *
   * @param id The game ID to remove.
   */
  suspend fun removeGame(id: Uuid)
}

/**
 * Returns the platform-specific database manager implementation.
 *
 * @return DatabaseManager instance for the current platform.
 */
expect fun getPlatformSpecificDatabaseManager(): DatabaseManager
