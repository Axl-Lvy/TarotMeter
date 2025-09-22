package proj.tarotmeter.axl.data

import proj.tarotmeter.axl.data.model.Game
import proj.tarotmeter.axl.data.model.Player
import proj.tarotmeter.axl.data.model.Round

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

  override suspend fun renamePlayer(id: Int, newName: String) {
    /* Nothing to do */
  }

  override suspend fun deletePlayer(id: Int) {
    /* Nothing to do */
  }

  override suspend fun getGames(): List<Game> {
    return emptyList()
  }

  override suspend fun getGame(id: Int): Game? {
    return null
  }

  override suspend fun insertGame(game: Game) {
    /* Nothing to do */
  }

  override suspend fun addRound(gameId: Int, round: Round) {
    /* Nothing to do */
  }

  override suspend fun removeGame(id: Int) {
    /* Nothing to do */
  }

  override suspend fun getMaxRoundId(): Int {
    return 0
  }

  override suspend fun getMaxPlayerId(): Int {
    return 0
  }

  override suspend fun getMaxGameId(): Int {
    return 0
  }
}
