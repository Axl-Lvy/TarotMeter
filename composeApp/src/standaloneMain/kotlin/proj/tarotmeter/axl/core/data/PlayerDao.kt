package proj.tarotmeter.axl.core.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlin.time.Instant
import kotlin.uuid.Uuid
import proj.tarotmeter.axl.core.data.entity.PlayerEntity

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
  @Query("SELECT * FROM PlayerEntity WHERE player_id = :id AND is_deleted = false")
  suspend fun getPlayer(id: Uuid): PlayerEntity?

  /**
   * Retrieves all players.
   *
   * @return List of all player entities.
   */
  @Query("SELECT * FROM PlayerEntity WHERE is_deleted = false")
  suspend fun getAllPlayers(): List<PlayerEntity>

  /**
   * Updates a player's name.
   *
   * @param id The player ID.
   * @param name The new name.
   * @return Number of rows affected.
   */
  @Query(
    "UPDATE PlayerEntity SET name = :name, updated_at = :now WHERE player_id = :id AND is_deleted = false"
  )
  suspend fun renamePlayer(id: Uuid, name: String, now: Instant)

  /**
   * Deletes a player by ID.
   *
   * @param id The player ID to delete.
   * @return Number of rows affected.
   */
  @Query(
    "UPDATE PlayerEntity SET is_deleted = true, updated_at = :now WHERE player_id = :id AND is_deleted = false"
  )
  suspend fun deletePlayer(id: Uuid, now: Instant)

  @Query("DELETE FROM PlayerEntity WHERE player_id = :id") suspend fun hardDeletePlayer(id: Uuid)

  /** Returns all players updated strictly after the provided instant (includes deleted). */
  @Query("SELECT * FROM PlayerEntity WHERE updated_at >= :since")
  suspend fun getPlayersUpdatedSince(since: Instant): List<PlayerEntity>

  @Query("DELETE FROM PlayerEntity") suspend fun clearPlayers()
}
