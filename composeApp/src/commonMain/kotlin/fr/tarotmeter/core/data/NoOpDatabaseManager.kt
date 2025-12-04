package fr.tarotmeter.core.data

import fr.tarotmeter.core.data.model.Game
import fr.tarotmeter.core.data.model.Player
import fr.tarotmeter.core.data.model.Round
import fr.tarotmeter.core.data.sync.GameSync
import fr.tarotmeter.core.data.sync.PlayerSync
import fr.tarotmeter.core.data.sync.RoundSync
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * No-operation DatabaseManager implementation that returns empty results. Used for platforms where
 * database functionality is not available.
 */
object NoOpDatabaseManager : DatabaseManager {
  // ...existing code...
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

  override suspend fun renameGame(id: Uuid, newName: String) {
    /* Nothing to do */
  }

  override suspend fun addRound(gameId: Uuid, round: Round) {
    /* Nothing to do */
  }

  override suspend fun deleteGame(id: Uuid) {
    /* Nothing to do */
  }

  override suspend fun updateRound(round: Round) {
    /* Nothing to do */
  }

  override suspend fun deleteRound(roundId: Uuid) {
    /* Nothing to do */
  }

  override suspend fun getPlayersUpdatedSince(since: Instant): List<PlayerSync> {
    return emptyList()
  }

  override suspend fun getGamesUpdatedSince(since: Instant): List<GameSync> {
    return emptyList()
  }

  override suspend fun getRoundsUpdatedSince(since: Instant): List<RoundSync> {
    return emptyList()
  }
}
