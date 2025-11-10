package proj.tarotmeter.axl.core.data

import co.touchlab.kermit.Logger
import kotlin.time.Instant
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import proj.tarotmeter.axl.core.data.cloud.Uploader
import proj.tarotmeter.axl.core.data.sync.GameSync
import proj.tarotmeter.axl.core.data.sync.PlayerSync
import proj.tarotmeter.axl.core.data.sync.RoundSync

abstract class LocalDatabaseManager : DatabaseManager, KoinComponent {
  private val uploader: Uploader by inject()

  protected fun notifyChange() {
    LOGGER.d { "Notifying changes" }
    uploader.notifyChange()
  }

  /** Return all players (including deleted) updated strictly after the given instant. */
  abstract suspend fun getPlayersUpdatedSince(since: Instant): List<PlayerSync>

  /** Return all games (including deleted) updated strictly after the given instant. */
  abstract suspend fun getGamesUpdatedSince(since: Instant): List<GameSync>

  /** Return all rounds (including deleted) updated strictly after the given instant. */
  abstract suspend fun getRoundsUpdatedSince(since: Instant): List<RoundSync>

  abstract suspend fun clear()

  /** Permanently removes all data marked as deleted from the local database. */
  abstract suspend fun cleanDeletedData(dateLimit: Instant)
}

private val LOGGER = Logger.withTag(LocalDatabaseManager::class.qualifiedName.toString())
