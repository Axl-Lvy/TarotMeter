package proj.tarotmeter.axl.core.data.cloud

import co.touchlab.kermit.Logger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.LocalDatabaseManager
import proj.tarotmeter.axl.core.data.sync.GameSync
import proj.tarotmeter.axl.core.data.sync.PlayerSync
import proj.tarotmeter.axl.core.data.sync.RoundSync

/**
 * Downloads data from the cloud database and stores it locally.
 *
 * Symmetrical to [Uploader], but inverts cloud/local roles:
 * - Cloud database acts as the source (remote state)
 * - Local database acts as the destination (target state)
 *
 * Uses incremental sync based on remote last_sync timestamp:
 * 1. Fetch the last remote sync timestamp for this device from the cloud.
 * 2. Fetch players, games, and rounds updated since that timestamp (including deleted items).
 * 3. Apply changes to the local database (insert/update or delete).
 * 4. Update the remote last_sync timestamp to prevent re-downloading the same data.
 *
 * In full sync mode, a complete refresh is performed:
 * - Clear local database.
 * - Fetch all remote data (non-deleted).
 * - Insert into local database.
 * - Update remote sync timestamp.
 */
class Downloader : KoinComponent {
  private val localDatabaseManager: LocalDatabaseManager by inject()
  private val cloudDatabaseManager: CloudDatabaseManager by inject()
  private val uploader: Uploader by inject()

  /**
   * Performs a download.
   *
   * @param fullSync Whether to perform a full refresh (default false for incremental sync).
   */
  suspend fun downloadData(fullSync: Boolean = false) {
    uploader.pauseUploadsDoing { doDownloadData(fullSync = fullSync) }
  }

  private suspend fun doDownloadData(fullSync: Boolean = false) {
    val deviceId = localDatabaseManager.getOrCreateDeviceId()

    if (fullSync) {
      fullRefreshLocalDatabase(deviceId)
    } else {
      appendOnlyRefresh(deviceId)
    }
  }

  private suspend fun fullRefreshLocalDatabase(deviceId: Uuid) {
    // Full refresh: fetch all remote state and replace local.
    val remotePlayers = runCatching { cloudDatabaseManager.getPlayers() }.getOrDefault(emptyList())
    val remoteGames = runCatching { cloudDatabaseManager.getGames() }.getOrDefault(emptyList())

    // Clear local database
    localDatabaseManager.clear()

    // Insert players first (games will reference them)
    remotePlayers.forEach { player -> runCatching { localDatabaseManager.insertPlayer(player) } }

    // Insert games with rounds
    remoteGames.forEach { game -> runCatching { localDatabaseManager.insertGame(game) } }

    // Update sync timestamps
    val maxUpdatedAt =
      listOfNotNull(
          remotePlayers.maxOfOrNull { it.updatedAt },
          remoteGames.maxOfOrNull { it.updatedAt },
          remoteGames.flatMap { it.rounds }.maxOfOrNull { it.updatedAt },
        )
        .maxOrNull()
    if (maxUpdatedAt != null) {
      cloudDatabaseManager.updateLastRemoteSync(deviceId, maxUpdatedAt + 1.milliseconds)
    }

    LOGGER.i { "Full sync completed. Players: ${remotePlayers.size}, Games: ${remoteGames.size}" }
  }

  private suspend fun appendOnlyRefresh(deviceId: Uuid) {
    // Incremental sync: fetch only updates since last remote sync
    val lastRemoteSync =
      runCatching { cloudDatabaseManager.getLastRemoteSync(deviceId) }
        .getOrElse {
          LOGGER.e { "Failed to get last remote sync: ${it.message}" }
          return
        }

    val players = cloudDatabaseManager.getPlayersUpdatedSince(lastRemoteSync)
    val games = cloudDatabaseManager.getGamesUpdatedSince(lastRemoteSync)
    val rounds = cloudDatabaseManager.getRoundsUpdatedSince(lastRemoteSync)

    if (players.isEmpty() && games.isEmpty() && rounds.isEmpty()) {
      LOGGER.d { "No remote changes to download since $lastRemoteSync" }
      return
    }

    refreshPlayers(players)

    refreshGames(games)

    refreshRounds(rounds)

    extractAndUpdateMaxUpdatedAt(players, games, rounds, deviceId)

    LOGGER.i {
      "Incremental sync completed. Players: ${players.size}, Games: ${games.size}, Rounds: ${rounds.size}"
    }
  }

  private suspend fun refreshPlayers(players: List<PlayerSync>) {
    players.forEach { playerSync ->
      if (playerSync.isDeleted) {
        runCatching { localDatabaseManager.deletePlayer(playerSync.id) }
      } else {
        runCatching { localDatabaseManager.insertPlayer(playerSync.toPlayer()) }
      }
    }
  }

  private suspend fun refreshGames(games: List<GameSync>) {
    games.forEach { gameSync ->
      if (gameSync.isDeleted) {
        runCatching { localDatabaseManager.deleteGame(gameSync.id) }
      } else {
        // Fetch full game data if needed to insert properly
        val game = cloudDatabaseManager.getGame(gameSync.id)
        if (game != null) {
          runCatching { localDatabaseManager.insertGame(game) }
        }
      }
    }
  }

  private suspend fun refreshRounds(rounds: List<RoundSync>) {
    // Apply rounds
    val localPlayers = localDatabaseManager.getPlayers()
    rounds.forEach { roundSync ->
      if (roundSync.isDeleted) {
        runCatching { localDatabaseManager.deleteRound(roundSync.id) }
      } else {
        val round =
          roundSync.toRound { playerId ->
            localPlayers.firstOrNull { it.id == playerId }
              ?: error("Player $playerId not found locally")
          }
        runCatching { localDatabaseManager.updateRound(round) }
      }
    }
  }

  private suspend fun extractAndUpdateMaxUpdatedAt(
    players: List<PlayerSync>,
    games: List<GameSync>,
    rounds: List<RoundSync>,
    deviceId: Uuid,
  ) {
    // Update sync timestamps
    val maxUpdatedAt =
      listOfNotNull(
          players.maxOfOrNull { it.updatedAt },
          games.maxOfOrNull { it.updatedAt },
          rounds.maxOfOrNull { it.updatedAt },
        )
        .maxOrNull()
    if (maxUpdatedAt != null) {
      cloudDatabaseManager.updateLastRemoteSync(deviceId, maxUpdatedAt + 1.milliseconds)
    }
  }

  suspend fun getPlayers() = localDatabaseManager.getPlayers()
}

private val LOGGER = Logger.withTag("Downloader")
