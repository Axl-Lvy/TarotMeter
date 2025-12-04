package fr.axllvy.tarotmeter.core.data

import androidx.room.*
import fr.axllvy.tarotmeter.core.data.entity.GameEntity
import fr.axllvy.tarotmeter.core.data.entity.GamePlayerCrossRef
import fr.axllvy.tarotmeter.core.data.entity.GameWithRefs
import fr.axllvy.tarotmeter.core.data.entity.RoundEntity
import fr.axllvy.tarotmeter.util.DateUtil
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** Data Access Object for Game operations. */
@Dao
interface GameDao {

  /**
   * Inserts a game into the database.
   *
   * @param game The game entity to insert.
   */
  @Upsert suspend fun insertGame(game: GameEntity)

  /**
   * Inserts a round into the database.
   *
   * @param round The round entity to insert.
   */
  @Upsert suspend fun insertRound(round: RoundEntity)

  /**
   * Inserts multiple rounds into the database.
   *
   * @param rounds Collection of round entities to insert.
   */
  @Upsert suspend fun insertRounds(rounds: Collection<RoundEntity>)

  /**
   * Inserts a game-player cross reference.
   *
   * @param crossRef The cross reference entity to insert.
   */
  @Upsert suspend fun insertGamePlayerCrossRef(crossRef: GamePlayerCrossRef)

  /**
   * Retrieves all games with their associated entities.
   *
   * @return List of games with all related data.
   */
  @Transaction
  @Query("SELECT * FROM GameEntity WHERE is_deleted = false")
  suspend fun getAllGames(): List<GameWithRefs>

  /**
   * Retrieves a specific game with its associated entities.
   *
   * @param id The game ID.
   * @return Game with all related data if found, null otherwise.
   */
  @Transaction
  @Query("SELECT * FROM GameEntity WHERE game_id = :id AND is_deleted = false")
  suspend fun getGame(id: Uuid): GameWithRefs?

  /**
   * Deletes a game by ID.
   *
   * @param id The game ID to delete.
   * @return Number of rows affected.
   */
  @Query(
    "UPDATE GameEntity SET is_deleted = true, updated_at = :now WHERE game_id = :id AND is_deleted = false"
  )
  suspend fun deleteGame(id: Uuid, now: Instant)

  /**
   * Renames a game by ID.
   *
   * @param id The game ID to rename.
   * @param newName The new name for the game.
   * @param now The current timestamp.
   */
  @Query(
    "UPDATE GameEntity SET name = :newName, updated_at = :now WHERE game_id = :id AND is_deleted = false"
  )
  suspend fun renameGame(id: Uuid, newName: String, now: Instant)

  /**
   * Deletes all games associated with a specific player.
   *
   * @param playerId The player ID.
   */
  @Query(
    "UPDATE GameEntity SET is_deleted = true, updated_at = :now WHERE game_id IN (SELECT game_id FROM GamePlayerCrossRef WHERE player_id = :playerId AND is_deleted = false) AND is_deleted = false"
  )
  suspend fun deleteGamesFromPlayer(playerId: Uuid, now: Instant)

  /**
   * Retrieves game IDs associated with a specific player.
   *
   * Careful, some games may be marked as deleted.
   *
   * @param playerId The player ID.
   * @return List of game IDs.
   */
  @Query(
    "SELECT game_id FROM GamePlayerCrossRef WHERE player_id = :playerId AND is_deleted = false"
  )
  suspend fun getGameIdsFromPlayer(playerId: Uuid): List<Uuid>

  /**
   * Deletes all games associated with a specific player.
   *
   * @param playerId The player ID.
   */
  @Query(
    "DELETE FROM GameEntity WHERE game_id IN (SELECT game_id FROM GamePlayerCrossRef WHERE player_id = :playerId)"
  )
  suspend fun hardDeleteGamesFromPlayer(playerId: Uuid)

  @Query("DELETE FROM GamePlayerCrossRef") suspend fun clearGamePlayerCrossRef()

  /** Returns games (including deleted) updated strictly after the instant. */
  @Query("SELECT * FROM GameEntity WHERE updated_at > :since")
  suspend fun getGamesUpdatedSince(since: Instant): List<GameEntity>

  /** Returns rounds (including deleted) updated strictly after the instant. */
  @Query("SELECT * FROM RoundEntity WHERE updated_at > :since")
  suspend fun getRoundsUpdatedSince(since: Instant): List<RoundEntity>

  /** Returns player ids for a game (non deleted cross refs). */
  @Query("SELECT player_id FROM GamePlayerCrossRef WHERE game_id = :gameId AND is_deleted = false")
  suspend fun getPlayerIdsForGame(gameId: Uuid): List<Uuid>

  /** Touch a game to update its updated_at field. */
  @Query("UPDATE GameEntity SET updated_at = :now WHERE game_id = :gameId")
  suspend fun touchGame(gameId: Uuid, now: Instant)

  /** Delete all games (for testing purposes). */
  @Query("DELETE FROM GameEntity") suspend fun clearGames()

  /** Marks a round as deleted. */
  @Query("UPDATE RoundEntity SET is_deleted = TRUE, updated_at = :now WHERE round_id = :roundId")
  suspend fun deleteRound(roundId: Uuid, now: Instant = DateUtil.now())

  /** Retrieves a specific round by ID. */
  @Query("SELECT * FROM RoundEntity WHERE round_id = :roundId")
  suspend fun getRound(roundId: Uuid): RoundEntity?

  @Query("UPDATE RoundEntity SET is_deleted = TRUE, updated_at = :now WHERE game_id = :gameId")
  suspend fun deleteRoundsForGame(gameId: Uuid, now: Instant = DateUtil.now())

  /** Permanently removes all games marked as deleted. */
  @Query("DELETE FROM GameEntity WHERE is_deleted = true AND updated_at <= :dateLimit")
  suspend fun cleanDeletedGames(dateLimit: Instant)

  /** Permanently removes all rounds marked as deleted. */
  @Query("DELETE FROM RoundEntity WHERE is_deleted = true AND updated_at <= :dateLimit")
  suspend fun cleanDeletedRounds(dateLimit: Instant)
}
