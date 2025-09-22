package proj.tarotmeter.axl.data

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

internal actual fun databaseBuilder(): RoomDatabase.Builder<StandaloneLocalDatabase> {
  val dbFilePath = documentDirectory() + "/tarot.db"
  return Room.databaseBuilder<StandaloneLocalDatabase>(name = dbFilePath)
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
  val documentDirectory =
    NSFileManager.defaultManager.URLForDirectory(
      directory = NSDocumentDirectory,
      inDomain = NSUserDomainMask,
      appropriateForURL = null,
      create = false,
      error = null,
    )
  return requireNotNull(documentDirectory?.path)
}
