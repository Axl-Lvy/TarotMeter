package fr.tarotmeter.core.data

import fr.tarotmeter.core.data.model.Game
import fr.tarotmeter.core.data.model.Player
import fr.tarotmeter.core.data.model.Round
import fr.tarotmeter.core.data.sync.GameSync
import fr.tarotmeter.core.data.sync.PlayerSync
import fr.tarotmeter.core.data.sync.RoundSync
import kotlin.time.Instant
import kotlin.uuid.Uuid

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
   * Renames an existing game.
   *
   * @param id The game's ID.
   * @param newName The new name for the game.
   */
  suspend fun renameGame(id: Uuid, newName: String)

  /**
   * Removes a game from the database.
   *
   * @param id The game ID to remove.
   */
  suspend fun deleteGame(id: Uuid)

  /**
   * Adds a round to an existing game.
   *
   * @param gameId The game ID.
   * @param round The round to add.
   */
  suspend fun addRound(gameId: Uuid, round: Round)

  /**
   * Deletes a round from the database.
   *
   * @param roundId The round ID to delete.
   */
  suspend fun deleteRound(roundId: Uuid)

  /**
   * Updates an existing round in the database.
   *
   * @param round The round to update.
   * @throws IllegalStateException If the round does not exist.
   */
  suspend fun updateRound(round: Round)

  /**
   * Return all players (including deleted) updated strictly after the given instant.
   *
   * @param since The instant to filter by.
   * @return List of players updated after the given instant.
   */
  suspend fun getPlayersUpdatedSince(since: Instant): List<PlayerSync>

  /**
   * Return all games (including deleted) updated strictly after the given instant.
   *
   * @param since The instant to filter by.
   * @return List of games updated after the given instant.
   */
  suspend fun getGamesUpdatedSince(since: Instant): List<GameSync>

  /**
   * Return all rounds (including deleted) updated strictly after the given instant.
   *
   * @param since The instant to filter by.
   * @return List of rounds updated after the given instant.
   */
  suspend fun getRoundsUpdatedSince(since: Instant): List<RoundSync>
}

/**
 * Returns the platform-specific database manager implementation.
 *
 * @return DatabaseManager instance for the current platform.
 */
expect fun getPlatformSpecificDatabaseManager(): DatabaseManager
