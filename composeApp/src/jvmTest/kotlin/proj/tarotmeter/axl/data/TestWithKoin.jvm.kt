package proj.tarotmeter.axl.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

actual fun getTestDatabaseManager(): DatabaseManager {
  return StandaloneLocalDatabaseManager(
    Room.inMemoryDatabaseBuilder<StandaloneLocalDatabase>()
      .setDriver(BundledSQLiteDriver())
      .setQueryCoroutineContext(Dispatchers.IO)
      .build()
  )
}
