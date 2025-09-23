package proj.tarotmeter.axl.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlin.uuid.Uuid
import proj.tarotmeter.axl.data.entity.PlayerEntity

/** Data Access Object for Player operations. */
@Dao
interface PlayerDao {
  /**
   * Inserts a player into the database.
   *
   * @param player The player entity to insert.
   */
  @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertPlayer(player: PlayerEntity)

  /**
   * Retrieves a player by ID.
   *
   * @param id The player ID.
   * @return The player entity if found, null otherwise.
   */
  @Query("SELECT * FROM PlayerEntity WHERE player_id = :id")
  suspend fun getPlayer(id: Uuid): PlayerEntity?

  /**
   * Retrieves all players.
   *
   * @return List of all player entities.
   */
  @Query("SELECT * FROM PlayerEntity") suspend fun getAllPlayers(): List<PlayerEntity>

  /**
   * Updates a player's name.
   *
   * @param id The player ID.
   * @param name The new name.
   * @return Number of rows affected.
   */
  @Query("UPDATE PlayerEntity SET name = :name WHERE player_id = :id")
  suspend fun renamePlayer(id: Uuid, name: String)

  /**
   * Deletes a player by ID.
   *
   * @param id The player ID to delete.
   * @return Number of rows affected.
   */
  @Query("DELETE FROM PlayerEntity WHERE player_id = :id") suspend fun deletePlayer(id: Uuid)
}
