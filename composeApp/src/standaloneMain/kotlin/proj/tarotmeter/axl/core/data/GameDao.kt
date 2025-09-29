package proj.tarotmeter.axl.core.data

import androidx.room.*
import kotlin.time.Instant
import kotlin.uuid.Uuid
import proj.tarotmeter.axl.core.data.entity.GameEntity
import proj.tarotmeter.axl.core.data.entity.GamePlayerCrossRef
import proj.tarotmeter.axl.core.data.entity.GameWithRefs
import proj.tarotmeter.axl.core.data.entity.RoundEntity

/** Data Access Object for Game operations. */
@Dao
interface GameDao {

  /**
   * Inserts a game into the database.
   *
   * @param game The game entity to insert.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertGame(game: GameEntity)

  /**
   * Inserts a round into the database.
   *
   * @param round The round entity to insert.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertRound(round: RoundEntity)

  /**
   * Inserts multiple rounds into the database.
   *
   * @param rounds Collection of round entities to insert.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRounds(rounds: Collection<RoundEntity>)

  /**
   * Inserts a game-player cross reference.
   *
   * @param crossRef The cross reference entity to insert.
   */
  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertGamePlayerCrossRef(crossRef: GamePlayerCrossRef)

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
   * Deletes all games associated with a specific player.
   *
   * @param playerId The player ID.
   */
  @Query(
    "UPDATE GameEntity SET is_deleted = true, updated_at = :now WHERE game_id IN (SELECT game_id FROM GamePlayerCrossRef WHERE player_id = :playerId AND is_deleted = false) AND is_deleted = false"
  )
  suspend fun deleteGamesFromPlayer(playerId: Uuid, now: Instant)

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

  @Query("DELETE FROM GameEntity") suspend fun clearGames()
}
