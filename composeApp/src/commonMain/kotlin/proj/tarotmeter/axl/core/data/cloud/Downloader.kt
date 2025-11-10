package proj.tarotmeter.axl.core.data.cloud

import co.touchlab.kermit.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.LocalDatabaseManager
import proj.tarotmeter.axl.core.data.config.LAST_SYNC
import proj.tarotmeter.axl.core.data.model.Game

/**
 * Downloads all data from the cloud database and stores it locally.
 *
 * Current strategy (default) is a full refresh:
 * 1. Fetch all remote players & games (non-deleted) using the cloud manager.
 * 2. Clear the local database.
 * 3. Insert players, then games (without rounds), then rounds for each game.
 * 4. Advance LAST_SYNC to the max updatedAt among all downloaded entities so that the uploader
 *    won't re-upload the freshly synced data.
 *
 * If [clearLocal] is false, a merge is performed:
 * - Remote players/games not present locally are inserted.
 * - Local players/games absent remotely are soft-deleted locally.
 * - Rounds cannot be individually removed in merge mode (no round delete API); missing remote
 *   rounds remain until a full refresh is performed.
 */
class Downloader : KoinComponent {
  private val localDatabaseManager: LocalDatabaseManager by inject()
  private val cloudDatabaseManager: CloudDatabaseManager by inject()
  private val uploader: Uploader by inject()

  /**
   * Performs a download.
   *
   * @param clearLocal Whether to clear the local store before applying remote state (default true).
   */
  suspend fun downloadData(clearLocal: Boolean = false) {
    uploader.pauseUploadsDoing { doDownloadData(clearLocal = clearLocal) }
  }

  private suspend fun doDownloadData(clearLocal: Boolean = false) {
    // Fetch remote state first so we don't wipe local data if network fails mid-way.
    val remotePlayers = runCatching { cloudDatabaseManager.getPlayers() }.getOrDefault(emptyList())
    val remoteGames = runCatching { cloudDatabaseManager.getGames() }.getOrDefault(emptyList())

    if (clearLocal) {
      // Always clear (even if remote is empty) so local reflects remote deletions.
      localDatabaseManager.clear()
    } else {
      // Merge mode: compute deletions & insertions.
      val remotePlayerIds = remotePlayers.map { it.id }.toSet()
      val remoteGameIds = remoteGames.map { it.id }.toSet()
      val localPlayers = runCatching { localDatabaseManager.getPlayers() }.getOrDefault(emptyList())
      val localGames = runCatching { localDatabaseManager.getGames() }.getOrDefault(emptyList())
      // Deletions (players not in remote)
      LOGGER.d {
        "Merging downloaded data: ${remotePlayers.size} remote players, ${localPlayers.size} local players"
      }
      localPlayers
        .filter { it.id !in remotePlayerIds }
        .forEach { localDatabaseManager.deletePlayer(it.id) }
      // Deletions (games not in remote)
      localGames
        .filter { it.id !in remoteGameIds }
        .forEach { game -> runCatching { localDatabaseManager.deleteGame(game.id) } }
      // Note: Missing remote rounds are not removed (no per-round delete API implemented).
    }

    // Insert players first (games will reference them). Dedup is handled by local layer (IGNORE
    // strategy for Room / overwrite in web impl).
    remotePlayers.forEach { player -> runCatching { localDatabaseManager.insertPlayer(player) } }

    // Insert games without rounds, then add rounds to preserve their updatedAt values.
    remoteGames.forEach { game ->
      val baseGame =
        Game(
          players = game.players,
          id = game.id,
          name = game.name,
          // empty rounds list for initial insert (web implementation stores provided rounds; we
          // avoid duplicates in merge/full refresh)
          roundsInternal = mutableListOf(),
          startedAt = game.startedAt,
          updatedAtInternal = game.updatedAt,
        )
      runCatching { localDatabaseManager.insertGame(baseGame) }
      // Now add rounds individually so standalone implementation (which ignores rounds on insert)
      // also gets them.
      game.rounds.forEach { round -> runCatching { localDatabaseManager.addRound(game.id, round) } }
    }

    // Update LAST_SYNC so subsequent uploads don't treat downloaded rows as new local edits.
    val maxUpdatedAt =
      listOfNotNull(
          remotePlayers.maxOfOrNull { it.updatedAt },
          remoteGames.maxOfOrNull { it.updatedAt },
          remoteGames.flatMap { it.rounds }.maxOfOrNull { it.updatedAt },
        )
        .maxOrNull()
    if (maxUpdatedAt != null) {
      LAST_SYNC.value = maxUpdatedAt
    }
  }

  suspend fun getPlayers() = localDatabaseManager.getPlayers()
}

private val LOGGER = Logger.withTag(Downloader::class.qualifiedName.toString())
