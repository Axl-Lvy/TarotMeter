package fr.tarotmeter.core.data.cloud

import co.touchlab.kermit.Logger
import fr.tarotmeter.core.data.LocalDatabaseManager
import fr.tarotmeter.core.data.cloud.auth.AuthManager
import fr.tarotmeter.core.data.config.LAST_SYNC
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Uploader : KoinComponent {
  private val cloudDatabaseManager: CloudDatabaseManager by inject()
  private val localDatabaseManager: LocalDatabaseManager by inject()
  private val authManager: AuthManager by inject()
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private val uploadMutex = Mutex()

  var forceDeactivate: Boolean = false

  val isActive
    get() = authManager.user != null && !forceDeactivate

  fun notifyChange(): Job {
    LOGGER.d { if (isActive) "Notifying changes" else "Uploader not active" }
    if (!isActive) {
      val dummyJob = Job(null)
      dummyJob.complete()
      return dummyJob
    }
    return scope.launch { triggerUpload() }
  }

  suspend fun pauseUploadsDoing(block: suspend () -> Unit) {
    uploadMutex.withLock {
      val oldDeactivateFlag = forceDeactivate
      forceDeactivate = true
      try {
        block()
      } finally {
        forceDeactivate = oldDeactivateFlag
      }
    }
  }

  private suspend fun triggerUpload() {
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
          // We add a small delta to avoid re-uploading items with the same timestamp
          LAST_SYNC.value = maxUpdatedAt + 1.milliseconds
          // Clean up deleted data after successful upload
          localDatabaseManager.cleanDeletedData(maxUpdatedAt)
        }
        LOGGER.i {
          "Uploaded data successfully. Players: ${players.size}, Games: ${games.size}, Rounds: ${rounds.size}"
        }
      }
      .onFailure { LOGGER.e { "Failed to upload data: ${it.message}" } }
  }
}

private val LOGGER = Logger.withTag("Uploader")
