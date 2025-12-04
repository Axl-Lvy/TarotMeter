package fr.axllvy.tarotmeter.core.data

import co.touchlab.kermit.Logger
import fr.axllvy.tarotmeter.core.data.cloud.Uploader
import kotlin.time.Instant
import kotlin.uuid.Uuid
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class LocalDatabaseManager : DatabaseManager, KoinComponent {
  private val uploader: Uploader by inject()

  protected fun notifyChange() {
    uploader.notifyChange()
  }

  /** Permanently removes all data from the local database. */
  abstract suspend fun clear()

  /** Permanently removes all data marked as deleted from the local database. */
  abstract suspend fun cleanDeletedData(dateLimit: Instant)

  suspend fun getOrCreateDeviceId(): Uuid {
    val existingId = getDeviceId()
    if (existingId != null) {
      return existingId
    }
    val newId = Uuid.random()
    insertDeviceId(newId)
    LOGGER.d { "Generated new device ID: $newId" }
    return newId
  }

  /** Inserts the device ID into the database. */
  protected abstract suspend fun insertDeviceId(deviceId: Uuid)

  /** Retrieves the device ID from the database, or null if not set. */
  protected abstract suspend fun getDeviceId(): Uuid?
}

private val LOGGER = Logger.withTag("LocalDatabaseManager")
