package proj.tarotmeter.axl.core.data.cloud

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.LocalDatabaseManager
import proj.tarotmeter.axl.core.data.config.LAST_SYNC

class Uploader : KoinComponent {
  private val cloudDatabaseManager: CloudDatabaseManager by inject()
  private val localDatabaseManager: LocalDatabaseManager by inject()

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  private val uploadMutex = Mutex()

  var isActive = false
    set(value) {
      field = value
      if (value) {
        notifyChange()
      }
    }

  fun notifyChange() {
    if (!isActive) return
    scope.launch { triggerUploadLoop() }
  }

  private suspend fun triggerUploadLoop() {
    uploadMutex.withLock { uploadUnsyncedData() }
  }

  private suspend fun uploadUnsyncedData() {
    val lastSync = LAST_SYNC.value
    val players = localDatabaseManager.getPlayersUpdatedSince(lastSync)
    val games = localDatabaseManager.getGamesUpdatedSince(lastSync)
    val rounds = localDatabaseManager.getRoundsUpdatedSince(lastSync)

    if (players.isEmpty() && games.isEmpty() && rounds.isEmpty()) return

    runCatching {
        // Order matters: players -> games (and cross refs) -> rounds
        cloudDatabaseManager.upsertPlayersSync(players)
        cloudDatabaseManager.upsertGamesSync(games)
        cloudDatabaseManager.upsertRoundsSync(rounds)
        // Only move the sync cursor if upload succeeded completely
        val maxUpdatedAt =
          listOfNotNull(
              players.maxOfOrNull { it.updatedAt },
              games.maxOfOrNull { it.updatedAt },
              rounds.maxOfOrNull { it.updatedAt },
            )
            .maxOrNull()
        if (maxUpdatedAt != null) {
          LAST_SYNC.value = maxUpdatedAt
        }
      }
      .onFailure {
        // Swallow: next notifyChange will retry with same LAST_SYNC
      }
  }
}
