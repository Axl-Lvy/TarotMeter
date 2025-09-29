package proj.tarotmeter.axl.core.data

import kotlin.uuid.Uuid
import proj.tarotmeter.axl.core.data.model.Game
import proj.tarotmeter.axl.core.data.model.Player
import proj.tarotmeter.axl.core.data.model.Round

/**
 * No-operation DatabaseManager implementation that returns empty results. Used for platforms where
 * database functionality is not available.
 */
object NoOpDatabaseManager : DatabaseManager {
  override suspend fun getPlayers(): List<Player> {
    return emptyList()
  }

  override suspend fun insertPlayer(player: Player) {
    /* Nothing to do */
  }

  override suspend fun renamePlayer(id: Uuid, newName: String) {
    /* Nothing to do */
  }

  override suspend fun deletePlayer(id: Uuid) {
    /* Nothing to do */
  }

  override suspend fun getGames(): List<Game> {
    return emptyList()
  }

  override suspend fun getGame(id: Uuid): Game? {
    return null
  }

  override suspend fun insertGame(game: Game) {
    /* Nothing to do */
  }

  override suspend fun addRound(gameId: Uuid, round: Round) {
    /* Nothing to do */
  }

  override suspend fun deleteGame(id: Uuid) {
    /* Nothing to do */
  }
}
