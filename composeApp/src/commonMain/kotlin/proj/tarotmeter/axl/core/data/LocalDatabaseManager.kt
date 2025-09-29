package proj.tarotmeter.axl.core.data

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
    uploader.notifyChange()
  }

  /** Return all players (including deleted) updated strictly after the given instant. */
  abstract suspend fun getPlayersUpdatedSince(since: Instant): List<PlayerSync>

  /** Return all games (including deleted) updated strictly after the given instant. */
  abstract suspend fun getGamesUpdatedSince(since: Instant): List<GameSync>

  /** Return all rounds (including deleted) updated strictly after the given instant. */
  abstract suspend fun getRoundsUpdatedSince(since: Instant): List<RoundSync>

  abstract suspend fun clear()
}
