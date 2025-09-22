package proj.tarotmeter.axl.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import proj.tarotmeter.axl.data.entity.GameEntity
import proj.tarotmeter.axl.data.entity.GamePlayerCrossRef
import proj.tarotmeter.axl.data.entity.GameWithRefs
import proj.tarotmeter.axl.data.entity.RoundEntity

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
  @Transaction @Query("SELECT * FROM GameEntity") suspend fun getAllGames(): List<GameWithRefs>

  /**
   * Retrieves a specific game with its associated entities.
   *
   * @param id The game ID.
   * @return Game with all related data if found, null otherwise.
   */
  @Transaction
  @Query("SELECT * FROM GameEntity WHERE game_id = :id")
  suspend fun getGame(id: Int): GameWithRefs?

  /**
   * Deletes a game by ID.
   *
   * @param id The game ID to delete.
   * @return Number of rows affected.
   */
  @Query("DELETE FROM GameEntity WHERE game_id = :id") suspend fun deleteGame(id: Int): Int

  /**
   * Gets the maximum game ID.
   *
   * @return The highest game ID, or null if no games exist.
   */
  @Query("SELECT MAX(game_id) FROM GameEntity") suspend fun getMaxGameId(): Int?

  /**
   * Gets the maximum round ID.
   *
   * @return The highest round ID, or null if no rounds exist.
   */
  @Query("SELECT MAX(round_id) FROM RoundEntity") suspend fun getMaxRoundId(): Int?

  /**
   * Deletes all games associated with a specific player.
   *
   * @param playerId The player ID.
   */
  @Query(
    "DELETE FROM GameEntity WHERE game_id IN (SELECT game_id FROM GamePlayerCrossRef WHERE player_id = :playerId)"
  )
  suspend fun deleteGamesFromPlayer(playerId: Int)
}
