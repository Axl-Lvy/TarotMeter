package proj.tarotmeter.axl.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlin.uuid.Uuid
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
  suspend fun getGame(id: Uuid): GameWithRefs?

  /**
   * Deletes a game by ID.
   *
   * @param id The game ID to delete.
   * @return Number of rows affected.
   */
  @Query("DELETE FROM GameEntity WHERE game_id = :id") suspend fun deleteGame(id: Uuid)

  /**
   * Deletes all games associated with a specific player.
   *
   * @param playerId The player ID.
   */
  @Query(
    "DELETE FROM GameEntity WHERE game_id IN (SELECT game_id FROM GamePlayerCrossRef WHERE player_id = :playerId)"
  )
  suspend fun deleteGamesFromPlayer(playerId: Uuid)
}
